package ru.yandex.qatools.embed.postgresql.distribution;

/**
 * PostgreSQL Version enum
 */
public enum PostgreSQLVersion implements de.flapdoodle.embed.process.distribution.Version {
    V16_3("16.3-1"),
    V16_0("16.3-1"),
    V15_7("15.7-1"),
    V15_4("15.4-1"),
    V14_12("14.12-1"),
    V14_9("14.9-1"),
    /**
     * 11 and 12 for Mac OS X and Windows x86-64 only because EnterpriseDB reduced the
     * <a href="https://www.enterprisedb.com/docs/en/11.0/PG_Inst_Guide_v11/PostgreSQL_Installation_Guide.1.04.html">supported platforms</a>
     * on their
     * <a href="https://www.enterprisedb.com/downloads/postgres-postgresql-downloads">binary download site</a>.
     */
    V13_15("13.15-1"),
    V13_12("13.12-1"),
    V13_2("13.1-1"),
    V12_19("12.19-1"),
    V12_16("12.16-1"),
    V12_5("12.5-1"),
    V12_2("12.2-1"),
    @Deprecated V11_21("11.21-1"),
    @Deprecated V11_10("11.10-1"),
    @Deprecated V11_7("11.7-1"),
    @Deprecated V11_2("11.2-1"),
    @Deprecated V10_23("10.23-1"),
    @Deprecated V10_15("10.15-1"),
    @Deprecated V10_12("10.12-1"),
    @Deprecated V10_7("10.7-1"),
    @Deprecated V9_6_24("9.6.24-1"),
    @Deprecated V9_6_20("9.6.20-1"),
    @Deprecated V9_6_17("9.6.17-1"),
    @Deprecated V9_6_12("9.6.12-1"),
    @Deprecated V9_5_24("9.5.24-1"),
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
        @Deprecated V9_5(V9_5_24),
        @Deprecated V9_6(V9_6_20),
        @Deprecated V9(V9_6_24),
        @Deprecated V10(V10_23),
        /**
         * 11 for Mac OS X and Windows x86-64 only because EnterpriseDB reduced the
         * <a href="https://www.enterprisedb.com/docs/en/11.0/PG_Inst_Guide_v11/PostgreSQL_Installation_Guide.1.04.html">supported platforms</a>
         * on their
         * <a href="https://www.enterprisedb.com/downloads/postgres-postgresql-downloads">binary download site</a>.
         */
        V11(V11_21),
        V12(V12_19),
        V13(V13_15),
        V14(V14_12),
        V15(V15_7),
        V16(V16_3),
        PRODUCTION(V16);

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
