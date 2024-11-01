package com.github.arc33.springsend.repository.fileoperation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileOperationEventRepositoryCustomImpl implements FileOperationEventRepositoryCustom {

    @PersistenceContext(unitName = "fileOperationsPersistenceUnit")
    private EntityManager entityManager;

    @Override
    public Map<String, Long> getOperationStats(Instant since) {
        String jpql = """
            SELECT e.operationType, COUNT(e)
            FROM FileOperationEvent e
            WHERE e.timestamp >= :since
            GROUP BY e.operationType
            """;

        Query query = entityManager.createQuery(jpql)
                .setParameter("since", since);

        List<Object[]> results = query.getResultList();
        Map<String, Long> stats = new HashMap<>();

        for (Object[] result : results) {
            stats.put(result[0].toString(), (Long) result[1]);
        }

        return stats;
    }
}