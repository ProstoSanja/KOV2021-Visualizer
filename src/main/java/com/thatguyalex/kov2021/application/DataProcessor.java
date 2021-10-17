package com.thatguyalex.kov2021.application;

import com.thatguyalex.kov2021.Presentation.classes.CountyData;
import com.thatguyalex.kov2021.Presentation.classes.PartyData;
import com.thatguyalex.kov2021.Presentation.classes.PersonData;
import com.thatguyalex.kov2021.infrastructure.DataProvider;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DataProcessor {

    @NonNull
    private DataProvider dataProvider;
    private PartyMappings partyMappings = new PartyMappings();

    private List<CountyData> countyData;
    private List<PersonData> peopleData;

    public List<CountyData> getCountyData() {
        countyData = dataProvider.getData().getData().getResultsInAdminUnit().stream().map(it -> {
            List<PartyData> partyData = null;
            if (it.getParties().getParty() != null) {
                partyData = it.getParties().getParty().stream().map(party -> new PartyData(party.getCode(), party.getVotesCount())).sorted(Comparator.comparingInt(PartyData::getVotes)).toList();
            }
            return CountyData.builder()
                    .id(it.getAdminUnit().getCode())
                    .totalVotes(it.getTotalVotes())
                    .leadingParty(partyData != null ? partyData.get(0).getName() : null)
                    .partyData(partyData)
                    .build();
        })
        .toList();
        return countyData;
    }

    public List<PersonData> getPeopleData() {
        peopleData = dataProvider.getData().getData().getResultsInAdminUnit().stream()
            .flatMap(it -> it.getElectedPersons().getPerson() != null ? it.getElectedPersons().getPerson().stream() : Stream.of())
            .map(person -> PersonData.builder()
                .name(person.getForename())
                .lastname(person.getSurname())
                .regNumber(person.getCandidateRegNumber())
                .totalVotes(person.getVotesCount())
                .partyName(partyMappings.getPartyShortCode(person.getPartyName()))
                .elected(person.isElected())
                .build())
            .sorted((f1, f2) -> Integer.compare(f2.getTotalVotes(), f1.getTotalVotes()))
            .toList();
        return peopleData.stream().limit(100).toList();
    }
}