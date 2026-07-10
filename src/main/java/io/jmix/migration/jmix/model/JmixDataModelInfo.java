package io.jmix.migration.jmix.model;

import java.util.List;

/**
 * Facts about the Jmix project data model gathered by the entity analyzer.
 */
public class JmixDataModelInfo {
    private final List<String> jpaEntities;
    private final List<String> dtoEntities;
    private final List<String> embeddables;
    private final List<String> enums;
    private final List<String> entityEventListeners;
    private final int javaxImportFilesCount;

    public JmixDataModelInfo(List<String> jpaEntities, List<String> dtoEntities, List<String> embeddables,
                             List<String> enums, List<String> entityEventListeners, int javaxImportFilesCount) {
        this.jpaEntities = List.copyOf(jpaEntities);
        this.dtoEntities = List.copyOf(dtoEntities);
        this.embeddables = List.copyOf(embeddables);
        this.enums = List.copyOf(enums);
        this.entityEventListeners = List.copyOf(entityEventListeners);
        this.javaxImportFilesCount = javaxImportFilesCount;
    }

    public List<String> getJpaEntities() {
        return jpaEntities;
    }

    public List<String> getDtoEntities() {
        return dtoEntities;
    }

    public List<String> getEmbeddables() {
        return embeddables;
    }

    public List<String> getEnums() {
        return enums;
    }

    public List<String> getEntityEventListeners() {
        return entityEventListeners;
    }

    /**
     * Number of Java files importing {@code javax.persistence}/{@code javax.validation}/
     * {@code javax.annotation}: the volume of the jakarta namespace sweep.
     */
    public int getJavaxImportFilesCount() {
        return javaxImportFilesCount;
    }
}
