package com.example.softdevoluciones.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.softdevoluciones.entity.ReturnDetail;

public interface ReturnDetailRepository extends JpaRepository<ReturnDetail, UUID> {

    @Query("""
            SELECT SUM(rd.quantity) FROM ReturnDetail AS rd
            WHERE rd.orderDetail.id = :orderDetailId
            AND rd.request.status IN (
                com.example.softdevoluciones.enums.ReturnStatus.APPROVED,
                com.example.softdevoluciones.enums.ReturnStatus.COMPLETED
            )""")
    Long findApprovedQuantityByOrderDetailId(@Param("orderDetailId") UUID orderDetailId);
}
