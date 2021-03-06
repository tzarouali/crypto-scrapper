/*
 * This file is generated by jOOQ.
 */
package cryptoscrapper.model.jooq.tables;


import cryptoscrapper.model.jooq.Keys;
import cryptoscrapper.model.jooq.Public;
import cryptoscrapper.model.jooq.tables.records.CoinDetailsRecord;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CoinDetails extends TableImpl<CoinDetailsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.coin_details</code>
     */
    public static final CoinDetails COIN_DETAILS = new CoinDetails();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CoinDetailsRecord> getRecordType() {
        return CoinDetailsRecord.class;
    }

    /**
     * The column <code>public.coin_details.coin_id</code>.
     */
    public final TableField<CoinDetailsRecord, Integer> COIN_ID = createField(DSL.name("coin_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.coin_details.rank</code>.
     */
    public final TableField<CoinDetailsRecord, Integer> RANK = createField(DSL.name("rank"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.coin_details.price_usd</code>.
     */
    public final TableField<CoinDetailsRecord, BigDecimal> PRICE_USD = createField(DSL.name("price_usd"), SQLDataType.NUMERIC.nullable(false), this, "");

    /**
     * The column <code>public.coin_details.created</code>.
     */
    public final TableField<CoinDetailsRecord, OffsetDateTime> CREATED = createField(DSL.name("created"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false), this, "");

    private CoinDetails(Name alias, Table<CoinDetailsRecord> aliased) {
        this(alias, aliased, null);
    }

    private CoinDetails(Name alias, Table<CoinDetailsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.coin_details</code> table reference
     */
    public CoinDetails(String alias) {
        this(DSL.name(alias), COIN_DETAILS);
    }

    /**
     * Create an aliased <code>public.coin_details</code> table reference
     */
    public CoinDetails(Name alias) {
        this(alias, COIN_DETAILS);
    }

    /**
     * Create a <code>public.coin_details</code> table reference
     */
    public CoinDetails() {
        this(DSL.name("coin_details"), null);
    }

    public <O extends Record> CoinDetails(Table<O> child, ForeignKey<O, CoinDetailsRecord> key) {
        super(child, key, COIN_DETAILS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<UniqueKey<CoinDetailsRecord>> getKeys() {
        return Arrays.<UniqueKey<CoinDetailsRecord>>asList(Keys.COIN_DETAILS_COIN_ID_CREATED_KEY);
    }

    @Override
    public List<ForeignKey<CoinDetailsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<CoinDetailsRecord, ?>>asList(Keys.COIN_DETAILS__COIN_FK);
    }

    public Coin coin() {
        return new Coin(this, Keys.COIN_DETAILS__COIN_FK);
    }

    @Override
    public CoinDetails as(String alias) {
        return new CoinDetails(DSL.name(alias), this);
    }

    @Override
    public CoinDetails as(Name alias) {
        return new CoinDetails(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public CoinDetails rename(String name) {
        return new CoinDetails(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CoinDetails rename(Name name) {
        return new CoinDetails(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, Integer, BigDecimal, OffsetDateTime> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
