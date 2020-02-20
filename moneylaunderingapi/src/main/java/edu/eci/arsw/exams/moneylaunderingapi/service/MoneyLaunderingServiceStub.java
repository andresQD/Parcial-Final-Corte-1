package edu.eci.arsw.exams.moneylaunderingapi.service;

import edu.eci.arsw.exams.moneylaunderingapi.model.SuspectAccount;
import java.util.ArrayList;

import java.util.List;

public class MoneyLaunderingServiceStub implements MoneyLaunderingService {

    @Override
    public void updateAccountStatus(SuspectAccount suspectAccount) {
        //TODO
    }

    @Override
    public SuspectAccount getAccountStatus(String accountId) {
        //TODO
        return null;
    }

    @Override
    public List<SuspectAccount> getSuspectAccounts() {
        //TODO
        List<SuspectAccount> suspectAccounts = new ArrayList<SuspectAccount>();
        
    return null;
    }
}
