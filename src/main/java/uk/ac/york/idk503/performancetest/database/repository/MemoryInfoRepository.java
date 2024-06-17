package uk.ac.york.idk503.performancetest.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.york.idk503.performancetest.database.dao.MemoryInfo;

@Repository
public interface MemoryInfoRepository extends JpaRepository<MemoryInfo, Long> {
    MemoryInfo findById(final long id);
}
