package kz.asembina.pvl_vuzy_bot.service.report;

public class TelegramResponse {
    public boolean ok;
    public Result result;

    public class Result{
        public String file_id;
        public String file_unique_id;
        public int file_size;
        public String file_path;
    }
}
