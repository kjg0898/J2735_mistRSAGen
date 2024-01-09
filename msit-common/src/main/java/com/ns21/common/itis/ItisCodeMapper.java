package com.ns21.common.itis;

/**
 * packageName    : com.ns21.common.itis
 * fileName       : ItisCodeMapper.java
 * author         : kjg08
 * date           : 2024-01-08
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-01-08        kjg08           최초 생성
 */
public class ItisCodeMapper {
    //Category 로 ITIS 코드 결정
    public static Integer getCategoryITISCode(String categoryName) {
        // categoryName을 기반으로 ITIS 코드를 반환합니다.
        // 해당 카테고리에 유효한 ITIS 코드가 없으면 null을 반환합니다.
        switch (categoryName) {
            case "dynamic_object.vehicle.truck":
                return ItisCodes.HAZARDOUS_MATERIAL_VEHICLE;
            case "dynamic_object.vehicle.personal_mobility":
                return ItisCodes.SLOW_VEHICLE;
            case "dynamic_object.vehicle.police_car":
                return ItisCodes.POLICE_CAR;
            case "dynamic_object.vehicle.ambulance":
                return ItisCodes.AMBULANCE;
            case "dynamic_object.vehicle.fire_truck":
                return ItisCodes.FIRE_TRUCK;
            case "dynamic_object.human.pedestrian":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "dynamic_object.human.police_officer":
                return ItisCodes.POLICE_CAR;
            case "dynamic_object.human.construction_worker":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "dynamic_object.human.firefighter":
                return ItisCodes.FIRE_TRUCK;
            case "dynamic_object.human.stroller":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "dynamic_object.human.wheelchair":
                return ItisCodes.PEDESTRIAN_ON_ROAD;

            case "movable_object.traffic_cone":
                return ItisCodes.MOBILE_CONSTRUCTION;
            case "movable_object.barrier":
                return ItisCodes.ROAD_CLOSURE_LANE_BLOCKAGE;
            case "movable_object.debris":
                return ItisCodes.FALLING_OBJECTS;
            case "movable_object.other_movable_object":
                return ItisCodes.FALLING_OBJECTS;
            default:
                return null;

        }
    }

    //vehicleState 로 ITIS 코드 결정
    public static Integer getAttributeITISCode(String vehicleState) {
        // 해당 차량 상태에 유효한 ITIS 코드가 없으면 null을 반환합니다.
        switch (vehicleState) {
            case "vehicle_state.moving":
                return ItisCodes.STOP_AND_GO_TRAFFIC;
            case "vehicle_state.stopped":
                return ItisCodes.STOPPED_VEHICLE;
            case "rider_state.with_rider":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "rider_state.without_rider":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "human_state.moving":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "human_state.standing":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "human_state.sitting":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "human_state.lying_down":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            default:
                return null; // 이 차량 상태에 유효한 ITIS 코드가 없음을 나타냅니다.
        }
    }


}