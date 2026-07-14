package com.mercury.mercury.Trade.specification;

import com.mercury.mercury.Trade.dto.TradeSearchRequest;
import com.mercury.mercury.Trade.entity.TradeEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TradeSpecification {
    public static Specification<TradeEntity> getTradeByFilters(TradeSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            if (request.getClientId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("client_id").get("clientID"), request.getClientId()));
            }

            if (request.getInstrumentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("instrument_id").get("instrumentID"), request.getInstrumentId()));
            }

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (request.getTradeType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("trade_type"), request.getTradeType()));
            }

            if (request.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("trade_date"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("trade_date"), request.getToDate()));
            }

            if (request.getMinQuantity() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("quantity"), request.getMinQuantity()));
            }
            if (request.getMaxQuantity() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), request.getMaxQuantity()));
            }

            if (request.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            if (request.getCreatedBy() != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy"), request.getCreatedBy()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
