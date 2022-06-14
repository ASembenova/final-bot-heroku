package kz.asembina.pvl_vuzy_bot.db.repo;

import kz.asembina.pvl_vuzy_bot.db.domain.Lang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LangRepo extends JpaRepository<Lang, Long> {

    Lang findByChatId(long chatId);

    List<Lang> findAll();
}
