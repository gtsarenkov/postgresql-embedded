package ru.yandex.qatools.embed.postgresql.distribution;

/**
 * PostgreSQL Version enum
 */
public enum PostgreSQLVersion implements de.flapdoodle.embed.process.distribution.Version {
    /**
     * 11 and 12 for Mac OS X and Windows x86-64 only because EnterpriseDB reduced the
     * <a href="https://www.enterprisedb.com/docs/en/11.0/PG_Inst_Guide_v11/PostgreSQL_Installation_Guide.1.04.html">supported platforms</a>
     * on their
     * <a href="https://www.enterprisedb.com/downloads/postgres-postgresql-downloads">binary download site</a>.
     */
    V12_2("12.2-1"),
    V11_7("11.7-1"),
    V11_2("11.2-1"),
    V10_12("10.12-1"),
    V10_7("10.7-1"),
    V9_6_17("9.6.17-1"),
    V9_6_12("9.6.12-1"),
    @Deprecated V9_5_21("9.5.21-1"),
    @Deprecated V9_5_16("9.5.16-1");

    private final String specificVersion;

    PostgreSQLVersion (String vName) {
        this.specificVersion = vName;
    }

    @Override
    public String asInDownloadPath() {
        return specificVersion;
    }

    @Override
    public String toString() {
        return "Version{" + specificVersion + '}';
    }

    public enum Main implements de.flapdoodle.embed.process.distribution.Version {
        @Deprecated V9_5(V9_5_21),
        V9_6(V9_6_17),
        V10(V10_12),
        PRODUCTION(V10_7),
        /**
         * 11 for Mac OS X and Windows x86-64 only because EnterpriseDB reduced the
         * <a href="https://www.enterprisedb.com/docs/en/11.0/PG_Inst_Guide_v11/PostgreSQL_Installation_Guide.1.04.html">supported platforms</a>
         * on their
         * <a href="https://www.enterprisedb.com/downloads/postgres-postgresql-downloads">binary download site</a>.
         */
        V11(V11_7),
        V12(V12_2);

        private final de.flapdoodle.embed.process.distribution.Version _latest;

        Main(de.flapdoodle.embed.process.distribution.Version latest) {
            _latest = latest;
        }

        @Override
        public String asInDownloadPath() {
            return _latest.asInDownloadPath();
        }
    }
}
