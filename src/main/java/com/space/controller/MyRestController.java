package com.space.controller;


import com.space.exception.ShipBadRequestException;
import com.space.exception.ShipNotFoundItemException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class MyRestController {

    @Autowired
    private ShipService shipService;

    @GetMapping("/ships")
    public List<Ship> findAllShips(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String planet,
            @RequestParam(required = false) ShipType shipType,
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Boolean isUsed,
            @RequestParam(required = false) Double minSpeed,
            @RequestParam(required = false) Double maxSpeed,
            @RequestParam(required = false) Integer minCrewSize,
            @RequestParam(required = false) Integer maxCrewSize,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) ShipOrder order,
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "3") Integer pageSize
    ) {
        List<Ship> ships =  shipService.findAllShips(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize,
                maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);
        return ships;
    }

    @GetMapping("/ships/count")
    public Integer findNumberOfShips(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String planet,
            @RequestParam(required = false) ShipType shipType,
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Boolean isUsed,
            @RequestParam(required = false) Double minSpeed,
            @RequestParam(required = false) Double maxSpeed,
            @RequestParam(required = false) Integer minCrewSize,
            @RequestParam(required = false) Integer maxCrewSize,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating
    ) {
        Integer shipsNumber =  shipService.findNumberOfShips(name, planet, shipType, after,
                before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating
        );
        return shipsNumber;
    }

    @GetMapping("/ships/{id}")
    public Ship getShipByID(@PathVariable Long id) {
        Ship ship =  shipService.findShipByID(id);
        if (ship == null) {
//            throw new ShipBadRequestException();
            throw new ShipNotFoundItemException();
        }
        return ship;
    }

    @PostMapping("/ships")
    public Ship createShip(@RequestBody Ship ship) {
        Ship createdShip =  shipService.createShip(ship);
        if (createdShip == null) {
            throw new ShipBadRequestException("test message");
        }
        return createdShip;
    }

    @DeleteMapping("/ships/{id}")
    public void deleteShip(@PathVariable Long id) {
        shipService.deleteShip(id);
    }

    @PutMapping("ships/{id}")
    public Ship updateShip(@PathVariable Long id, @RequestBody Ship ship) {
        Ship updatedShip = shipService.updateShip(id, ship);
        if (updatedShip == null) {
            throw new ShipBadRequestException();
        }
        return updatedShip;
    }
}
