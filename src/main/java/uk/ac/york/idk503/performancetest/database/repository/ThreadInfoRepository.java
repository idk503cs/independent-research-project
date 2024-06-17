package uk.ac.york.idk503.performancetest.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.york.idk503.performancetest.database.dao.ThreadInfo;

@Repository
public interface ThreadInfoRepository extends JpaRepository<ThreadInfo, Long> {
    ThreadInfo findById(final long id);
}
