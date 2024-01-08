package com.ns21.rsa.creator;

import com.ns21.common.enums.j2735.J2735MessageID;
import com.ns21.common.itis.ItisCodes;
import com.ns21.common.mist.codec.J2735ToJson;
import com.ns21.common.mist.codec.JsonToJ2735;
import com.ns21.common.mist.parser.MetaDataExtracting;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * packageName    : com.ns21.rsa.creator
 * fileName       : RsaMessageCreator.java
 * author         : kjg08
 * date           : 2023-11-24
 * description    :JSON 처리 및 열거형과 코덱 처리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-24        kjg08           최초 생성
 */
public class RsaMessageCreator extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(RsaMessageCreator.class);
    private static int currentIndex = 0;
    private List<String> jsonMessages;
    private long timerId; // 클래스 레벨 변수로 선언

    @Override
    public void start()  {
        // 메타데이터 추출 및 처리
        MetaDataExtracting extractor = new MetaDataExtracting();
        extractor.processFiles().thenRun(() -> {
            logger.info("----------------------  Reading and parsing metadata Please wait . . .  ----------------------");
            // 메시지 목록은 한 번만 생성
            try {
                jsonMessages = RsaValueCreator.createRsaMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.info("----------------------  The task is complete  ----------------------");
            logger.info("Total messages generated: {}", jsonMessages.size());

            // 모든 메시지 처리 후 타이머 중지를 위한 핸들러 ID 저장
            timerId = vertx.setPeriodic(1000, id -> {
                if (!jsonMessages.isEmpty() && currentIndex < jsonMessages.size()) {
                    // 현재 인덱스에 따라 하나의 메시지를 선택합니다.
                    String jsonMessage = jsonMessages.get(currentIndex);
                    logger.info("Sending message at index: {}", currentIndex);

                    // 메시지 처리 로직
                    processMessage(jsonMessage);

                    // 현재 인덱스를 증가시키고, 모든 메시지 처리 시 타이머 중지
                    currentIndex++;
                    if (currentIndex >= jsonMessages.size()) {
                        logger.info("All messages sent. Stopping timer.");
                        vertx.cancelTimer(timerId);
                    }
                }
            });
        });
    }

    private void processMessage(String jsonMessage) {
        try {
            // 메시지 아이디 추가 및 변환 로직
            JsonObject originalJsonObject = new JsonObject(jsonMessage);
            JsonObject newJsonObject = new JsonObject();
            newJsonObject.put("messageId", J2735MessageID.RSA.getId());
            newJsonObject.put("value", originalJsonObject);
            jsonMessage = newJsonObject.toString();

            // JSON을 J2735 형식으로 변환하고 다시 JSON으로 변환
            String j2735Result = JsonToJ2735.convertToJ2735(jsonMessage);
            String jsonResult = J2735ToJson.convertToJSON(j2735Result);

            // 로그 출력
            logger.info("------------------------------------- convert to J2735 -------------------------------------");
            logger.info("convertToJ2735 : {}", j2735Result);
            logger.info("------------------------------------- convert to Json -------------------------------------");
            logger.info("convertToJson : {}", jsonResult);
        } catch (Exception e) {
            logger.error("Error in message processing", e);
        }
    }


    public static List<Integer> ITISRSACodeGen(String categoryName, String vehicleState) {
        List<Integer> itisCodes = new ArrayList<>();

        // categoryName에 대한 로직
        if (categoryName != null && !categoryName.equals("0")) {
            Integer categoryCode = getCategoryITISCode(categoryName);
            if (categoryCode != null) { // 유효한 코드가 있을 경우에만 추가
                itisCodes.add(categoryCode);
            }
        }
        // vehicleState에 대한 로직
        if (vehicleState != null && !vehicleState.equals("0")) {
            Integer vehicleStateCode = getVehicleStateITISCode(vehicleState);
            if (vehicleStateCode != null) { // 유효한 코드가 있을 경우에만 추가
                itisCodes.add(vehicleStateCode);
            }
        }
        return itisCodes;
    }


    // 특정 ITIS 코드를 결정하기 위한 예시 보조 메서드들
    private static Integer getCategoryITISCode(String categoryName) {
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
    private static Integer getVehicleStateITISCode(String vehicleState) {
        // vehicleState을 기반으로 ITIS 코드를 반환합니다.
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




//
  //          public static List<Integer> ITISRSACodeGen(String categoryName, String vehicleState) {
//
  //      categoryName = (categoryName != null) ? categoryName : "0";
  //      vehicleState = (vehicleState != null) ? vehicleState : "0";
//
  //      if (categoryName != null && !categoryName.equals("0")) {
  //      //instance_categoryName의 값은 하기와 같다
  //      switch (categoryName) {
  //          case "dynamic_object.vehicle.truck":
  //              itisCodes.add(ItisCodes.HAZARDOUS_MATERIAL_VEHICLE);
  //              break;
  //          case "dynamic_object.human.pedestrian":
  //              itisCodes.add(ItisCodes.PEDESTRIAN_ON_ROAD);
  //              break;
  //          case "movable_object.barrier":
  //              itisCodes.add(ItisCodes.ROAD_CLOSURE_LANE_BLOCKAGE);
  //              break;
  //          case "movable_object.traffic_cone":
  //              itisCodes.add(ItisCodes.MOBILE_CONSTRUCTION);
  //              break;
  //          default:
  //              itisCodes.add(0); // categoryName이 없거나 일치하지 않을 때
  //              break;
  //      }
  //  }
  //      if (vehicleState != null && !vehicleState.equals("0")) {
  //          //frameAnnotation_attribute 의 vehicle_state 값은 하기와 같다
  //          //stopped
  //          //moving
  //          switch (vehicleState) {
  //              case "moving":
  //                  itisCodes.add(ItisCodes.STOP_AND_GO_TRAFFIC);
  //                  break;
  //              case "stopped":
  //                  itisCodes.add(ItisCodes.STOPPED_VEHICLE);
  //                  break;
  //              default:
  //                  itisCodes.add(0); // vehicleState가 없거나 일치하지 않을 때
  //                  break;
  //          }
  //      }
  //      return itisCodes;
  //  }

}
