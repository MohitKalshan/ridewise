package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Rider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class RiderService {

    private final List<Rider> riders = new ArrayList<>();

    public Rider registerRider(String name, double latitude, double longitude) {
        Rider rider = new Rider(name, latitude, longitude);
        riders.add(rider);
        return rider;
    }

    public Optional<Rider> findById(String riderId) {
        return riders.stream()
                .filter(r -> r.getId().equals(riderId))
                .findFirst();
    }

    public List<Rider> getAllRiders() {
        return Collections.unmodifiableList(riders);
    }
}
