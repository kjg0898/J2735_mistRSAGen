package com.ns21.rsa.creator;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ns21.common.mist.parser.UuidMatching;
import com.ns21.common.util.MetaDataConvertUtil;

import java.io.IOException;
import java.util.*;

import static com.ns21.common.util.MetaDataConvertUtil.*;
import static com.ns21.rsa.creator.RsaMessageCreator.ITISRSACodeGen;

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
public class RsaValueCreator {
    private static final int UTM_ZONE = 52; // Assuming this is a constant
    private static int msgCnt = 0;

    public static List<String> createRsaMessage() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> messages = new ArrayList<>();
        // UuidMatching 클래스를 사용하여 관련된 데이터의 JSON 문자열을 가져옵니다.
        List<String> relatedUuids = UuidMatching.UuidPut();

        //  relatedUuids에는 연관된 데이터의 JSON 문자열이 포함되어 있습니다.
        for (String uuidJson : relatedUuids) {
            try {
                // JSON 문자열을 Map 객체로 변환합니다.
                Map<String, Object> uuidMap = mapper.readValue(uuidJson, new TypeReference<>() {
                });
                // Construct the JSON message
                Map<String, Object> rsaMessage = new LinkedHashMap<>();

                // Double 형태로 변환한 후 longValue() 메소드를 사용 EgoPoseDto 객체에서 타임스탬프 가져와서 타임스탬프를 연도와 시간으로 변환
                String timestampStr = String.valueOf(uuidMap.get("egoPose_timestamp"));
                long timestamp = (timestampStr != null && !timestampStr.isEmpty()) ? (long) Double.parseDouble(timestampStr) : 0;
                //timestamp 값 분 단위,integer 타입, 527040 범위 안으로 변경하여 가져오기
                int minutesOfYear = minuteOfTheYear(Long.toString(timestamp));

                // EgoPoseDto 객체에서 translation 가져와서  위경고도로 변환
                // 위치 및 방향 정보를 추가하는 부분에서의 타입 안전성 확인
                if (uuidMap.get("egoPose_translation") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Double> translation = (List<Double>) uuidMap.get("egoPose_translation");
                    double utmX = translation.get(0); // ex)326865.27824246883
                    double utmY = translation.get(1); // ex)4147694.5101196766
                    double elevation = translation.get(2); // ex)49.126053147017956
                    long[] utmToLatLon = MetaDataConvertUtil.CoordinateConverter.utmToLatLon(utmX, utmY, UTM_ZONE, elevation);

                    // sensor.json 의 rotation 값을 quaternionToHeading 를 통해 변환함 (ffff)
                    Object sensorRotationObj = uuidMap.get("sensor_rotation");
                    String heading = (sensorRotationObj instanceof List) ?
                            MetaDataConvertUtil.quaternionToHeading((List<Double>) sensorRotationObj) :
                            "0";  //  기본값

                    // 위치 및 방향 정보를 추가합니다.
                    Map<String, Object> position = new LinkedHashMap<>();

                    // 공통 필드를 추가합니다.
                    rsaMessage.put("msgCnt", msgCnt);
                    msgCnt = (msgCnt + 1) % 128;  // msgCnt가 127을 넘지 않도록 함
                    rsaMessage.put("timeStamp", minutesOfYear);

                    // "description" 필드에 ITIScode 값을 할당합니다. 주의할 차량과 차량의 이동상태,가시성 낮음 추가해야함 itis 코드를 이용하여 로직 추가해야함 instance.json 의 category_name , frame_annotation.json 의  vehicle_state , frameannotation의 visibility_level
                    rsaMessage.put("typeEvent", 0);
                    String categoryName = null;
                    String vehicleState = null;
                    Integer visibilityLevel = null;
                    if (uuidMap.containsKey("instance_categoryName")) {
                        categoryName = (String) uuidMap.get("instance_categoryName");  // instance.json에서 추출
                    } else {
                        categoryName = "0";
                    }
                    if (uuidMap.containsKey("frameAnnotation_attribute") && uuidMap.get("frameAnnotation_attribute") != null) {
                        Map<String, Object> frameAnnotationAttribute = (Map<String, Object>) uuidMap.get("frameAnnotation_attribute"); // frame_annotation.json에서 추출
                        vehicleState = (String) frameAnnotationAttribute.get("vehicle_state");
                    } else {
                        vehicleState = "0";
                    }
                    if (uuidMap.containsKey("frameAnnotation_visibilityLevel")&& uuidMap.get("frameAnnotation_visibilityLevel") != null) {
                        visibilityLevel = (Integer) uuidMap.get("frameAnnotation_visibilityLevel"); // frame_annotation.json에서 추출
                    } else {
                        visibilityLevel = 0;
                    }
                    List<Integer> descriptionValues = getDescriptionValuesFromSomeSource(categoryName, vehicleState, visibilityLevel);
                    rsaMessage.put("description", descriptionValues);  //  여기까지가 ITIScode 할당 하는 로직


                    rsaMessage.put("priority", "01");
                    rsaMessage.put("heading", heading);   //sensor.json 의 rotation

                    // convertTimestampToUtcMap에는 문자열 형태의 타임스탬프를 전달
                    position.put("utcTime", convertTimestampToUtcMap(Long.toString(timestamp)));
                    position.put("long", utmToLatLon[1]);  // ego_pose.json 파일의 translation,
                    position.put("lat", utmToLatLon[0]);  // ego_pose.json 파일의 translation,
                    position.put("elevation", utmToLatLon[2]);  // ego_pose.json 파일의 translation,
                    rsaMessage.put("position", position);
                    List<Map<String, Object>> regional = new ArrayList<>();
                    Map<String, Object> regionalItem = new LinkedHashMap<>();
                    regionalItem.put("regionId", 4);

                    Map<String, Object> regExtValue = new LinkedHashMap<>();
                    Map<String, Object> cits = new LinkedHashMap<>();
                    cits.put("stopID", uuidMap.get("log_location"));//log.json 의 location 로그가 캡처된 위치/명칭
                    cits.put("text", uuidMap.get("frameData_uuid"));  //frameData.json 의 uuid
                    cits.put("sendUniqueId", uuidMap.get("sensor_name")); //sensor.json 의 name // 어떤 장비로 인지 하였는지
                    regExtValue.put("cits", cits);

                    regionalItem.put("regExtValue", regExtValue);
                    regional.add(regionalItem);

                    rsaMessage.put("regional", regional);

                    // 최종적인 RSA 메시지를 구성합니다.
                    messages.add(mapper.writeValueAsString(rsaMessage));
                }
            } catch (ClassCastException e) {
                // 타입 캐스팅 예외 처리 로직
                e.printStackTrace();
            } catch (NullPointerException e) {
                // 널 포인터 예외 처리 로직
                e.printStackTrace();
            }
        }
        return messages; // 여러 메시지를 리스트로 반환
    }

    public static List<Integer> getDescriptionValuesFromSomeSource(
            String categoryName, String vehicleState, Integer visibilityLevel) {
        // ITIS 코드 변환 함수를 호출하여 descriptionValues를 생성합니다.
        return ITISRSACodeGen(categoryName, vehicleState, visibilityLevel);
    }

}
