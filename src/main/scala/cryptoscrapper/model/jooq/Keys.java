/*
 * This file is generated by jOOQ.
 */
package cryptoscrapper.model.jooq;


import cryptoscrapper.model.jooq.tables.Coin;
import cryptoscrapper.model.jooq.tables.CoinDetails;
import cryptoscrapper.model.jooq.tables.records.CoinDetailsRecord;
import cryptoscrapper.model.jooq.tables.records.CoinRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<CoinRecord> COIN_PKEY = Internal.createUniqueKey(Coin.COIN, DSL.name("coin_pkey"), new TableField[] { Coin.COIN.ID }, true);
    public static final UniqueKey<CoinRecord> COIN_SYMBOL_KEY = Internal.createUniqueKey(Coin.COIN, DSL.name("coin_symbol_key"), new TableField[] { Coin.COIN.SYMBOL }, true);
    public static final UniqueKey<CoinDetailsRecord> COIN_DETAILS_COIN_ID_CREATED_KEY = Internal.createUniqueKey(CoinDetails.COIN_DETAILS, DSL.name("coin_details_coin_id_created_key"), new TableField[] { CoinDetails.COIN_DETAILS.COIN_ID, CoinDetails.COIN_DETAILS.CREATED }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<CoinDetailsRecord, CoinRecord> COIN_DETAILS__COIN_FK = Internal.createForeignKey(CoinDetails.COIN_DETAILS, DSL.name("coin_fk"), new TableField[] { CoinDetails.COIN_DETAILS.COIN_ID }, Keys.COIN_PKEY, new TableField[] { Coin.COIN.ID }, true);
}
