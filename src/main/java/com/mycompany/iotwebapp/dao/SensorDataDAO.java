package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.SensorData;
import com.mycompany.iotwebapp.model.SensorType;
import com.mycompany.iotwebapp.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class SensorDataDAO {

    /**
     * INSERT 1 RECORD
     */
    public Long insert(SensorData data) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            // Gán timestamp nếu chưa có
            if (data.getCreatedAt() == null) {
                data.setCreatedAt(LocalDateTime.now());
            }

            em.persist(data);
            tx.commit();

            return data.getDataId();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error inserting sensor data", e);
        } finally {
            em.close();
        }
    }


    /**
     * INSERT BATCH (hiệu suất cao)
     */
    public int insertBatch(List<SensorData> dataList) {
        if (dataList == null || dataList.isEmpty()) return 0;

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (SensorData data : dataList) {

                if (data.getCreatedAt() == null) {
                    data.setCreatedAt(LocalDateTime.now());
                }

                em.persist(data);

                // Flush mỗi 20 record để tránh tràn bộ nhớ
                if (++count % 20 == 0) {
                    em.flush();
                    em.clear();
                }
            }

            tx.commit();
            return dataList.size();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error inserting batch", e);
        } finally {
            em.close();
        }
    }


    /**
     * Lấy X record mới nhất của một thiết bị
     */
    public List<SensorData> findLatestByDevice(Integer deviceId, int limit) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String jpql = "SELECT sd FROM SensorData sd " +
                    "WHERE sd.deviceId = :deviceId " +
                    "ORDER BY sd.createdAt DESC";

            TypedQuery<SensorData> query = em.createQuery(jpql, SensorData.class);
            query.setParameter("deviceId", deviceId);
            query.setMaxResults(limit);

            List<SensorData> results = query.getResultList();
            System.out.println("[SensorDataDAO] Found " + results.size() + " sensor data records for deviceId: " + deviceId);

            // Load SensorType info
            SensorTypeDAO typeDAO = new SensorTypeDAO();
            for (SensorData sd : results) {
                SensorType st = typeDAO.findById(sd.getSensorTypeId());
                if (st != null) {
                    sd.setSensorName(st.getSensorName());
                    sd.setUnit(st.getUnit());
                    System.out.println("[SensorDataDAO] Loaded sensor info: typeId=" + sd.getSensorTypeId() + ", name='" + st.getSensorName() + "', value=" + sd.getValue());
                } else {
                    System.out.println("[SensorDataDAO] Warning: SensorType not found for typeId: " + sd.getSensorTypeId());
                }
            }

            return results;

        } finally {
            em.close();
        }
    }


    /**
     * Lấy bản ghi mới nhất theo loại cảm biến (1 sensor)
     */
    public SensorData findLatestBySensorType(Integer deviceId, Integer sensorTypeId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String jpql = "SELECT sd FROM SensorData sd " +
                    "WHERE sd.deviceId = :deviceId AND sd.sensorTypeId = :typeId " +
                    "ORDER BY sd.createdAt DESC";

            TypedQuery<SensorData> query = em.createQuery(jpql, SensorData.class);
            query.setParameter("deviceId", deviceId);
            query.setParameter("typeId", sensorTypeId);
            query.setMaxResults(1);

            List<SensorData> list = query.getResultList();

            return list.isEmpty() ? null : list.get(0);

        } finally {
            em.close();
        }
    }


    /**
     * Query theo thời gian
     */
    public List<SensorData> findByTimeRange(Integer deviceId, LocalDateTime start, LocalDateTime end) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String jpql = "SELECT sd FROM SensorData sd " +
                    "WHERE sd.deviceId = :deviceId " +
                    "AND sd.createdAt BETWEEN :start AND :end " +
                    "ORDER BY sd.createdAt DESC";

            TypedQuery<SensorData> query = em.createQuery(jpql, SensorData.class);
            query.setParameter("deviceId", deviceId);
            query.setParameter("start", start);
            query.setParameter("end", end);

            return query.getResultList();

        } finally {
            em.close();
        }
    }


    /**
     * Đếm số record của thiết bị
     */
    public long countByDevice(Integer deviceId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String jpql = "SELECT COUNT(sd) FROM SensorData sd WHERE sd.deviceId = :deviceId";

            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("deviceId", deviceId);

            return query.getSingleResult();

        } finally {
            em.close();
        }
    }


    /**
     * Xóa các record quá cũ
     */
    public int deleteOlderThan(LocalDateTime before) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int deleted = em.createQuery(
                            "DELETE FROM SensorData sd WHERE sd.createdAt < :beforeDate")
                    .setParameter("beforeDate", before)
                    .executeUpdate();

            tx.commit();
            return deleted;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting old sensor data", e);

        } finally {
            em.close();
        }
    }
}
