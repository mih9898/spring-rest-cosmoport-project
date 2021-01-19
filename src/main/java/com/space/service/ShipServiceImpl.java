package com.space.service;

import com.space.controller.ShipOrder;
import com.space.exception.ShipBadRequestException;
import com.space.exception.ShipNotFoundItemException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShipServiceImpl implements ShipService {

    @Autowired
    private ShipRepository shipRepository;
    public static final int CURRENT_YEAR = 3019;

    @Override
    @Transactional
    public List<Ship> findAllShips(
            String name,
            String planet,
            ShipType shipType,
            Long after,
            Long before,
            Boolean isUsed,
            Double minSpeed,
            Double maxSpeed,
            Integer minCrewSize,
            Integer maxCrewSize,
            Double minRating,
            Double maxRating,
            ShipOrder order,
            Integer pageNumber,
            Integer pageSize
    ) {
        List<Ship> ships = shipRepository.findAll();
        ships = filterShipList(ships, name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        ships = filterShipsDisplay(ships, order, pageNumber, pageSize);
        return ships;
    }

    @Override
    @Transactional
    public Ship findShipByID(Long id) {
        if (id < 1) {
            throw new ShipBadRequestException();
        } else if (!shipRepository.existsById(id)) {
            throw new ShipNotFoundItemException();
        }
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Integer findNumberOfShips(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating) {
        List<Ship> ships = shipRepository.findAll();
        ships = filterShipList(ships, name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        return ships.size();
    }

    @Transactional
    @Override
    public Ship updateShip( Long id, Ship newShip) {
        Ship shipUpdate = findShipByID(id);

        if (newShip == null || shipUpdate == null) {
             throw new ShipNotFoundItemException();
        }
        if (newShip.getName() != null) {
            if (newShip.getName().length() > 50 ||
                    newShip.getName().isEmpty()) {
               throw new ShipBadRequestException();
            }
            shipUpdate.setName(newShip.getName());
        }
        if (newShip.getPlanet() != null) {
            if (newShip.getPlanet().length() > 50 ||
                    newShip.getPlanet().isEmpty()) {
                 throw new ShipBadRequestException();
            }
            shipUpdate.setPlanet(newShip.getPlanet());
        }
        if (newShip.getShipType() != null) {
            shipUpdate.setShipType(newShip.getShipType());
        }
        if (newShip.getProdDate() != null) {
            if (newShip.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() < 2800 ||
                    newShip.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() > 3019) {
                throw new ShipBadRequestException();
            }
            shipUpdate.setProdDate(newShip.getProdDate());
        }
        if (newShip.getUsed() != null) {
            shipUpdate.setUsed(newShip.getUsed());
        }
        if (newShip.getSpeed() != null) {
            if (newShip.getSpeed() < 0.01d ||
                    newShip.getSpeed() > 0.99d) {
                throw new ShipBadRequestException();
            }
            shipUpdate.setSpeed(newShip.getSpeed());
        }
        if (newShip.getCrewSize() != null) {
            if (newShip.getCrewSize() < 1 ||
                    newShip.getCrewSize() > 9999) {
                throw new ShipBadRequestException();
            }
            shipUpdate.setCrewSize(newShip.getCrewSize());
        }

        shipUpdate.setRating(calculateRating(shipUpdate));
        return shipRepository.save(shipUpdate);
    }
    @Override
    public Ship createShip(Ship ship) {
        if (
                ship.getName() == null
                || ship.getName().length() > 50
                || ship.getName().isEmpty()
                || ship.getPlanet() == null
                || ship.getPlanet().isEmpty()
                || ship.getPlanet().length() > 50
                || ship.getShipType() == null
                || ship.getProdDate() == null
                || !(isShipYearInBorders(ship.getProdDate()))
                || ship.getSpeed() == null
                || roundToHundreds(ship.getSpeed()) < 0.01
                || roundToHundreds(ship.getSpeed()) > 0.99
                || ship.getCrewSize() == null
                || ship.getCrewSize() < 1
                || ship.getCrewSize() > 9999
        ) {
            return null;
        } else if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        ship.setSpeed(roundToHundreds(ship.getSpeed()));
        ship.setRating(calculateRating(ship));
        return shipRepository.save(ship);
    }

    @Override
    public void deleteShip(Long id) {
//        Ship ship = findShipByID(id);
        if (id < 1) {
            throw new ShipBadRequestException();
        }
         if (!shipRepository.existsById(id)) {
            throw new ShipNotFoundItemException();
        }
        shipRepository.deleteById(id);
    }

    private Double calculateRating(Ship ship) {
        double speed = ship.getSpeed();
        double coefficientUsed = ship.getUsed() ? 0.5d : 1.0d;
        int currentYear = 3019;
        int productionDate = ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
        double rating = (80 * speed * coefficientUsed) / (double) (currentYear - productionDate + 1);
        return roundToHundreds(rating);
    }




    private boolean isShipYearInBorders(Date prodDate) {
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(prodDate);
        int shipYear = calDate.get(Calendar.YEAR);
        return shipYear >= 2800 && shipYear <= 3019 && prodDate.getTime() >= 0;
    }

    private List<Ship> filterShipsDisplay(List<Ship>ships, ShipOrder order, Integer pageNumber, Integer pageSize) {
        ships.sort(getOrderComparator(order));
//       return ships.subList(pageNumber * pageSize, ships.size() - 1)
//               .subList(0, pageSize);
        return ships.stream()
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

    }

    private Comparator<Ship> getOrderComparator(ShipOrder order) {
        if (order == null) {
            order = ShipOrder.ID;
        }
        String fieldName = order.getFieldName();
        Comparator<Ship> shipComparator = null;
        switch (fieldName) {
            case "id":
                shipComparator = Comparator.comparing(Ship::getId);
                break;
            case "speed":
                shipComparator = Comparator.comparing(Ship::getSpeed);
                break;
            case "prodDate":
                shipComparator = Comparator.comparing(Ship::getProdDate);
                break;
            case "rating":
                shipComparator = Comparator.comparing(Ship::getRating);
                break;
        }
        return shipComparator;
    }

    private  Double roundToHundreds(double n) {
        BigDecimal instance = new BigDecimal(Double.toString(n));
        instance = instance.setScale(2, RoundingMode.HALF_UP);
        return instance.doubleValue();
    }

    private List<Ship> filterShipList(List<Ship> ships, String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                      Integer maxCrewSize, Double minRating, Double maxRating) {

        if (name != null) {
            ships = ships.stream()
                    .filter(s -> s.getName().contains(name))
                    .collect(Collectors.toList());
        }

        if (planet != null) {
            ships = ships.stream()
                    .filter(s -> s.getPlanet().contains(planet))
                    .collect(Collectors.toList());
        }

        if (shipType != null) {
            ships = ships.stream()
                    .filter(s -> s.getShipType().equals(shipType))
                    .collect(Collectors.toList());
        }

        if (after != null) {
            ships = ships.stream()
                    .filter(s -> s.getProdDate().after(new Date(after)))
                    .collect(Collectors.toList());
        }

        if (before != null) {
            ships = ships.stream()
                    .filter(s -> s.getProdDate().before(new Date(before)))
                    .collect(Collectors.toList());
        }

        if (isUsed != null) {
            ships = ships.stream()
                    .filter(s -> s.getUsed() == isUsed)
                    .collect(Collectors.toList());
        }

        if (minSpeed != null) {
            ships = ships.stream()
                    .filter(s -> s.getSpeed() >= minSpeed)
                    .collect(Collectors.toList());
        }

        if (maxSpeed != null) {
            ships = ships.stream()
                    .filter(s -> s.getSpeed() <= maxSpeed)
                    .collect(Collectors.toList());
        }

        if (minCrewSize != null) {
            ships = ships.stream()
                    .filter(s -> s.getCrewSize() >= minCrewSize)
                    .collect(Collectors.toList());
        }

        if (maxCrewSize != null) {
            ships = ships.stream()
                    .filter(s -> s.getCrewSize() <= maxCrewSize)
                    .collect(Collectors.toList());
        }

        if (minRating != null) {
            ships = ships.stream()
                    .filter(s -> s.getRating() >= minRating)
                    .collect(Collectors.toList());
        }

        if (maxRating != null) {
            ships = ships.stream()
                    .filter(s -> s.getRating() <= maxRating)
                    .collect(Collectors.toList());
        }
        return ships;
    }

}
