    package sfu.mu_scanner;

    public class ErrologInfo {

        private String _id;
        private String ERR_PAGE_NAME;
        private String ERR_FUNCTION;
        private String ERR_MESSAGE;
        private String ERR_CAUSE;
        private String ERR_CRON;
        private String ERR_STACKTRACE;

        public String getERR_STACKTRACE() {
            return ERR_STACKTRACE;
        }


        public void setERR_STACKTRACE(String ERR_STACKTRACE) {
            this.ERR_STACKTRACE = ERR_STACKTRACE;
        }
        public String getERR_PAGE_NAME() {
            return ERR_PAGE_NAME;
        }



        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public void setERR_PAGE_NAME(String ERR_PAGE_NAME) {
            this.ERR_PAGE_NAME = ERR_PAGE_NAME;
        }

        public String getERR_FUNCTION() {
            return ERR_FUNCTION;
        }

        public void setERR_FUNCTION(String ERR_FUNCTION) {
            this.ERR_FUNCTION = ERR_FUNCTION;
        }

        public String getERR_MESSAGE() {
            return ERR_MESSAGE;
        }

        public void setERR_MESSAGE(String ERR_MESSAGE) {
            this.ERR_MESSAGE = ERR_MESSAGE;
        }

        public String getERR_CAUSE() {

            if (ERR_CAUSE == null)
                return "";
            return ERR_CAUSE;
        }

        public void setERR_CAUSE(String ERR_CAUSE) {
            this.ERR_CAUSE = ERR_CAUSE;
        }

        public String getERR_CRON() {
            return ERR_CRON;
        }

        public void setERR_CRON(String ERR_CRON) {
            this.ERR_CRON = ERR_CRON;
        }

    }

