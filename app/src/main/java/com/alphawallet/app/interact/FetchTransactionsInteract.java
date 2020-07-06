package com.alphawallet.app.interact;

import com.alphawallet.app.entity.ActivityMeta;
import com.alphawallet.app.entity.ContractType;
import com.alphawallet.app.entity.NetworkInfo;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.entity.tokens.TokenInfo;
import com.alphawallet.app.entity.Transaction;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.repository.TokenRepositoryType;
import com.alphawallet.app.repository.TransactionRepositoryType;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import com.alphawallet.app.service.TokensService;

import java.util.List;

public class FetchTransactionsInteract {

    private final TransactionRepositoryType transactionRepository;
    private final TokenRepositoryType tokenRepository;

    public FetchTransactionsInteract(TransactionRepositoryType transactionRepository,
                                     TokenRepositoryType tokenRepositoryType) {
        this.transactionRepository = transactionRepository;
        this.tokenRepository = tokenRepositoryType;
    }

    public Single<ActivityMeta[]> fetchTransactionMetas(Wallet wallet, List<Integer> networkFilters, long fetchTime, int fetchLimit) {
        return transactionRepository
                .fetchCachedTransactionMetas(wallet, networkFilters, fetchTime, fetchLimit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<ContractType> queryInterfaceSpec(TokenInfo tokenInfo)
    {
        //can resolve erc20, erc721 and erc875 from a getbalance check and look at decimals. Otherwise try more esoteric
        return tokenRepository.determineCommonType(tokenInfo);
    }

    public Transaction fetchCached(String walletAddress, String hash)
    {
        return transactionRepository.fetchCachedTransaction(walletAddress, hash);
    }

    public Realm getRealmInstance(Wallet wallet)
    {
        return transactionRepository.getRealmInstance(wallet);
    }

    public Single<ActivityMeta[]> fetchTransactionMetas(Wallet wallet, int chainId, String tokenAddress, int historyCount)
    {
        return transactionRepository
                .fetchCachedTransactionMetas(wallet, chainId, tokenAddress, historyCount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
