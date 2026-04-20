package com.stockwatch.repository;

import com.stockwatch.model.AlertLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {


    List<AlertLog> findByProductIdOrderBySentAtDesc(Long productId);


    List<AlertLog> findTop20ByOrderBySentAtDesc();
    @Transactional
    @Modifying
    void deleteByProductId(Long id);
}
