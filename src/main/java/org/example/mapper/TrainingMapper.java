package org.example.mapper;

import org.example.dto.request.CreateTrainingRequest;
import org.example.dto.response.TrainerTrainingResponse;
import org.example.dto.response.TrainingResponse;
import org.example.dto.response.TrainingTypeResponse;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface TrainingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "traineeName", ignore = true)
    @Mapping(target = "trainerName", ignore = true)
    @Mapping(target = "trainingType", ignore = true)
    Training toModel(CreateTrainingRequest request);

    TrainingResponse toTrainingResponse(Training training);

    List<TrainingResponse> toTrainingResponseList(List<Training> trainings);

    TrainerTrainingResponse toTrainerTrainingResponse(Training training);

    List<TrainerTrainingResponse> toTrainerTrainingResponseList(List<Training> trainings);

    @Mapping(source = "trainingTypeName", target = "trainingType")
    TrainingTypeResponse toTrainingTypeResponse(TrainingType trainingType);

    List<TrainingTypeResponse> toTrainingTypeResponseList(List<TrainingType> trainingTypes);

    default String mapTrainingType(TrainingType trainingType) {
        return trainingType != null ? trainingType.trainingTypeName() : null;
    }
}