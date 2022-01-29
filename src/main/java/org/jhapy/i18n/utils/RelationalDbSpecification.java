package org.jhapy.i18n.utils;

import org.jhapy.i18n.domain.BaseEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelationalDbSpecification<T extends BaseEntity> implements Specification<T> {
  private final List<RelationalDbSearchCriteria> list = new ArrayList<>();

  private final List<RelationalDbSearchCriteria[]> orList = new ArrayList<>();

  public RelationalDbSpecification() {}

  public RelationalDbSpecification(RelationalDbSearchCriteria criteria) {
    this.list.add(criteria);
  }

  public void add(RelationalDbSearchCriteria criteria) {
    list.add(criteria);
  }

  public void or(RelationalDbSearchCriteria... criterias) {
    orList.add(criterias);
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    query.distinct(true);
    // create a new predicate list
    List<Predicate> predicates = toPredicateList(list, root, query, builder);

    // add add criteria to predicates
    orList.forEach(
        relationalDbSearchCriteria -> {
          List<Predicate> orPredicates =
              toPredicateList(
                  Arrays.stream(relationalDbSearchCriteria).toList(), root, query, builder);
          predicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
        });
    return builder.and(predicates.toArray(new Predicate[0]));
  }

  protected List<Predicate> toPredicateList(
      List<RelationalDbSearchCriteria> list,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    List<Predicate> predicates = new ArrayList<>();
    for (RelationalDbSearchCriteria criteria : list) {
      Expression keyColumn;
      if (criteria.getKey().contains(".")) {
        String[] keys = criteria.getKey().split("\\.");
        var mapRoot = root.join(keys[0], JoinType.LEFT);
        keyColumn = mapRoot.get(keys[1]);
      } else {
        keyColumn = root.get(criteria.getKey());
      }
      if (criteria.getOperation().equals(RelationalDbSearchOperation.GREATER_THAN)) {
        predicates.add(builder.greaterThan(keyColumn, criteria.getValue().toString()));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.LESS_THAN)) {
        predicates.add(builder.lessThan(keyColumn, criteria.getValue().toString()));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.GREATER_THAN_EQUAL)) {
        predicates.add(builder.greaterThanOrEqualTo(keyColumn, criteria.getValue().toString()));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.LESS_THAN_EQUAL)) {
        predicates.add(builder.lessThanOrEqualTo(keyColumn, criteria.getValue().toString()));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.NOT_EQUAL)) {
        predicates.add(builder.notEqual(keyColumn, criteria.getValue()));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.EQUAL)) {
        predicates.add(builder.equal(keyColumn, criteria.getValue()));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.MATCH)) {
        predicates.add(
            builder.like(
                builder.lower(keyColumn),
                "%" + criteria.getValue().toString().toLowerCase() + "%"));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.MATCH_END)) {
        predicates.add(
            builder.like(
                builder.lower(keyColumn), criteria.getValue().toString().toLowerCase() + "%"));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.MATCH_START)) {
        predicates.add(
            builder.like(
                builder.lower(keyColumn), "%" + criteria.getValue().toString().toLowerCase()));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.IN)) {
        predicates.add(builder.in(keyColumn).value(criteria.getValue()));
      } else if (criteria.getOperation().equals(RelationalDbSearchOperation.NOT_IN)) {
        predicates.add(builder.not(keyColumn).in(criteria.getValue()));
      }
    }
    return predicates;
  }
}
