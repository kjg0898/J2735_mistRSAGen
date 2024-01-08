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
            case "dynamic_object.human.pedestrian":
                return ItisCodes.PEDESTRIAN_ON_ROAD;
            case "movable_object.barrier":
                return ItisCodes.ROAD_CLOSURE_LANE_BLOCKAGE;
            case "movable_object.traffic_cone":
                return ItisCodes.MOBILE_CONSTRUCTION;
            default:
                return null;

        }
    }
    //vehicleState 로 ITIS 코드 결정
    public static Integer getVehicleStateITISCode(String vehicleState) {
        // 해당 차량 상태에 유효한 ITIS 코드가 없으면 null을 반환합니다.
        switch (vehicleState) {
            case "moving":
                return ItisCodes.STOP_AND_GO_TRAFFIC;
            case "stopped":
                return ItisCodes.STOPPED_VEHICLE;
            default:
                return null; // 이 차량 상태에 유효한 ITIS 코드가 없음을 나타냅니다.
        }
    }
}
