package com.karoldm.bookstore.filters;

import com.karoldm.bookstore.dto.requests.BooksFilterDTO;
import com.karoldm.bookstore.entities.Book;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class BooksFilters implements Specification<Book> {
    private BooksFilterDTO booksFilterDTO;
    private UUID storeId;

    @Override
    public Predicate toPredicate(
            Root<Book> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(root.get("store").get("id"), storeId));

        if (StringUtils.hasText(booksFilterDTO.getTitle())) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + booksFilterDTO.getTitle().toLowerCase() + "%"
            ));
        }

        if (StringUtils.hasText(booksFilterDTO.getAuthor())) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("author")),
                    "%" + booksFilterDTO.getAuthor().toLowerCase() + "%"
            ));
        }

        if (booksFilterDTO.getAvailable() != null) {
            predicates.add(criteriaBuilder.equal(root.get("available"),
                    booksFilterDTO.getAvailable()));
        }

        if (booksFilterDTO.getRating() != null) {
            predicates.add(criteriaBuilder.equal(root.get("rating"),
                    booksFilterDTO.getRating()));
        }

        if (booksFilterDTO.getStartDate() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("releasedAt"),
                    booksFilterDTO.getStartDate()));
        }

        if (booksFilterDTO.getEndDate() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("releasedAt"),
                    booksFilterDTO.getEndDate()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
