package com.ebookstore.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractFileRepository<T, ID> {

    protected final ObjectMapper objectMapper;
    protected final Path filePath;
    protected final Class<T> entityClass;
    protected final Function<T, ID> idExtractor;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public AbstractFileRepository(String storageDir, String fileName, Class<T> entityClass, Function<T, ID> idExtractor) {
        this.entityClass = entityClass;
        this.idExtractor = idExtractor;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Path dir = Paths.get(storageDir != null ? storageDir : "data");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + dir, e);
        }

        this.filePath = dir.resolve(fileName);
        initFileIfNotExists();
    }

    private void initFileIfNotExists() {
        lock.writeLock().lock();
        try {
            if (!Files.exists(filePath)) {
                objectMapper.writeValue(filePath.toFile(), new ArrayList<T>());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<T> findAll() {
        lock.readLock().lock();
        try {
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass);
            List<T> list = objectMapper.readValue(filePath.toFile(), type);
            return list != null ? new ArrayList<>(list) : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from file: " + filePath, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<T> findById(ID id) {
        if (id == null) return Optional.empty();
        return findAll().stream()
                .filter(entity -> id.equals(idExtractor.apply(entity)))
                .findFirst();
    }

    public List<T> findBy(Predicate<T> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    public synchronized T save(T entity) {
        lock.writeLock().lock();
        try {
            List<T> entities = findAll();
            ID id = idExtractor.apply(entity);
            int index = -1;
            for (int i = 0; i < entities.size(); i++) {
                if (id.equals(idExtractor.apply(entities.get(i)))) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                entities.set(index, entity);
            } else {
                entities.add(entity);
            }

            objectMapper.writeValue(filePath.toFile(), entities);
            return entity;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public synchronized List<T> saveAll(List<T> newEntities) {
        lock.writeLock().lock();
        try {
            List<T> entities = findAll();
            for (T entity : newEntities) {
                ID id = idExtractor.apply(entity);
                int index = -1;
                for (int i = 0; i < entities.size(); i++) {
                    if (id.equals(idExtractor.apply(entities.get(i)))) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    entities.set(index, entity);
                } else {
                    entities.add(entity);
                }
            }
            objectMapper.writeValue(filePath.toFile(), entities);
            return entities;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save entities to file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public synchronized boolean deleteById(ID id) {
        lock.writeLock().lock();
        try {
            List<T> entities = findAll();
            boolean removed = entities.removeIf(entity -> id.equals(idExtractor.apply(entity)));
            if (removed) {
                objectMapper.writeValue(filePath.toFile(), entities);
            }
            return removed;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete from file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public synchronized void deleteAll() {
        lock.writeLock().lock();
        try {
            objectMapper.writeValue(filePath.toFile(), new ArrayList<T>());
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public long count() {
        return findAll().size();
    }
}
