package io.axoniq.demo.sqwebinar.readside;

import io.axoniq.demo.sqwebinar.commandside.IssuedEvent;
import io.axoniq.demo.sqwebinar.commandside.RedeemedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Component
@XSlf4j
@RequiredArgsConstructor
public class CardSummaryProjection {

    private final EntityManager entityManager;
    private final QueryUpdateEmitter queryUpdateEmitter;

    @EventHandler
    public void on(IssuedEvent event) {
        log.trace("projecting {}", event);
        entityManager.persist(new CardSummary(event.getId(), event.getAmount(), event.getAmount()));

        queryUpdateEmitter.emit(
                CountCardSummariesQuery.class,
                query -> event.getId().startsWith(query.getFilter().getIdStartsWith()),
                new CardCountChangedUpdate()
        );
    }

    @EventHandler
    public void on(RedeemedEvent event) {
        log.trace("projecting {}", event);
        CardSummary summary = entityManager.find(CardSummary.class, event.getId());
        summary.setRemainingValue(summary.getRemainingValue().subtract(event.getAmount()));
        queryUpdateEmitter.emit(
                FetchCardSummariesQuery.class,
                query -> event.getId().startsWith(query.getFilter().getIdStartsWith()),
                summary
        );
    }

    @QueryHandler
    public List<CardSummary> handle(FetchCardSummariesQuery query) {
        log.trace("handling {}", query);
        TypedQuery<CardSummary> jpaQuery = entityManager.createNamedQuery(
                "CardSummary.fetch", CardSummary.class);
        jpaQuery.setParameter("idStartsWith", query.getFilter().getIdStartsWith());
        jpaQuery.setFirstResult(query.getOffset());
        jpaQuery.setMaxResults(query.getLimit());
        return log.exit(jpaQuery.getResultList());
    }

    @QueryHandler
    public Integer handle(CountCardSummariesQuery query) {
        log.trace("handling {}", query);
        TypedQuery<Long> jpaQuery = entityManager.createNamedQuery(
                "CardSummary.count", Long.class);
        jpaQuery.setParameter("idStartsWith", query.getFilter().getIdStartsWith());
        return log.exit(jpaQuery.getSingleResult().intValue());
    }

}
