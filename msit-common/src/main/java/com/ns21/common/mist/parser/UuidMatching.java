package com.ns21.common.mist.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ns21.common.exception.JsonToJ2735Exception;
import com.ns21.common.mist.dto.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * fileName       : UuidMatching.java
 * author         : kjg08
 * date           : 2023-11-23
 * description    :JSON 처리와 관련된 UUID 처리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-23        kjg08           최초 생성
 */
public class UuidMatching {

    public static List<String> UuidPut() {
        ObjectMapper mapper = new ObjectMapper();
        List<String> UuidList = new ArrayList<>();

        // Retrieve data from DataStorage
        List<DatasetDto> datasetList = DataStorage.getInstance().getDatasets();
        List<EgoPoseDto> egoPoseList = DataStorage.getInstance().getEgoPoses();
        List<FrameDto> frameList = DataStorage.getInstance().getFrames();
        List<FrameAnnotationDto> frameAnnotationList = DataStorage.getInstance().getFrameAnnotations();
        List<InstanceDto> instanceList = DataStorage.getInstance().getInstances();
        List<LogDto> logList = DataStorage.getInstance().getLogs();
        List<SensorDto> sensorList = DataStorage.getInstance().getSensors();
        List<FrameDataDto> frameDataList = DataStorage.getInstance().getFrameData();

        // Create maps for quick UUID lookup
        Map<String, DatasetDto> datasetMap = createMapFromList(datasetList);
        Map<String, EgoPoseDto> egoPoseMap = createMapFromList(egoPoseList);
        Map<String, FrameDto> frameMap = createMapFromList(frameList);
        Map<String, InstanceDto> instanceMap = createMapFromList(instanceList);
        Map<String, LogDto> logMap = createMapFromList(logList);
        Map<String, SensorDto> sensorMap = createMapFromList(sensorList);


        for (FrameDataDto frameData : frameDataList) {
            FrameDto frame = frameMap.get(frameData.getFrameUuid());
            EgoPoseDto egoPose = egoPoseMap.get(frameData.getEgoPoseUuid());
            SensorDto sensor = sensorMap.get(frameData.getSensorUuid());

            if (frame != null && egoPose != null && sensor != null) {
                DatasetDto dataset = frame != null ? datasetMap.get(frame.getDatasetUuid()) : null;
                LogDto log = dataset != null ? logMap.get(dataset.getLogUuid()) : null;
                List<FrameAnnotationDto> relevantFrameAnnotations = frameAnnotationList.stream()
                        .filter(fa -> fa.getFrameDataUuid().equals(frameData.getUuid()))
                        .collect(Collectors.toList());

                for (FrameAnnotationDto frameAnnotation : relevantFrameAnnotations) {
                    InstanceDto instance = instanceMap.get(frameAnnotation.getInstanceUuid());
                    if (log != null && instance != null) {
                        Map<String, Object> uuid = new LinkedHashMap<>();
                        try {
                            uuid = new LinkedHashMap<>(createPrefixedMap(dataset, "dataset"));
                        } catch (JsonToJ2735Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            uuid.putAll(createPrefixedMap(egoPose, "egoPose"));
                        } catch (JsonToJ2735Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            uuid.putAll(createPrefixedMap(sensor, "sensor"));
                        } catch (JsonToJ2735Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            uuid.putAll(createPrefixedMap(frame, "frame"));
                        } catch (JsonToJ2735Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            uuid.putAll(createPrefixedMap(frameData, "frameData"));
                        } catch (JsonToJ2735Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            uuid.putAll(createPrefixedMap(frameAnnotation, "frameAnnotation"));
                        } catch (JsonToJ2735Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            uuid.putAll(createPrefixedMap(instance, "instance"));
                        } catch (JsonToJ2735Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            uuid.putAll(createPrefixedMap(log, "log"));
                        } catch (JsonToJ2735Exception e) {
                            throw new RuntimeException(e);
                        }

                        try {
                            UuidList.add(mapper.writeValueAsString(uuid));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        };
        return UuidList;
    }

    private static <T extends BaseDto> Map<String, T> createMapFromList(List<T> list) {
        return list.stream().collect(Collectors.toMap(T::getUuid, Function.identity(), (a, b) -> a));
    }
//데이터 세트 UUID의 경우 "dataset_uuid"
    private static Map<String, Object> createPrefixedMap(Object dto, String prefix) throws JsonToJ2735Exception {
        Map<String, Object> prefixedMap = new LinkedHashMap<>();
        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                    // 문자열 필드의 경우 빈 문자열이면 "0"으로 설정
                    if (field.getType().equals(String.class)) {
                        value = "0";
                    } else if (field.getType().equals(int.class) || field.getType().equals(double.class) || field.getType().equals(long.class)) {
                        // 숫자 필드의 경우 0으로 설정
                        value = 0;
                    }
                }
                prefixedMap.put(prefix + "_" + field.getName(), value);
            } catch (IllegalAccessException e) {
                throw new JsonToJ2735Exception("Error accessing field in DTO: " + prefix, e);
            }
        }
        return prefixedMap;
    }
}
