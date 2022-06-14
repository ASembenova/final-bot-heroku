package kz.asembina.pvl_vuzy_bot.db.repo;

import kz.asembina.pvl_vuzy_bot.db.domain.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface DocumentRepo extends JpaRepository<GeneratedDocument, Long> {

    Set<GeneratedDocument> findAllByChatId(long chatId);
}
