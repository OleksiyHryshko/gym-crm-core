package org.example.mapper;

import org.example.dto.request.TraineeRegistrationRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeProfileResponse;
import org.example.dto.response.TrainerSummaryResponse;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface TraineeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    Trainee toModel(TraineeRegistrationRequest request);


    @Mapping(source = "request.firstName", target = "firstName")
    @Mapping(source = "request.lastName", target = "lastName")
    @Mapping(source = "request.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "request.address", target = "address")
    @Mapping(source = "request.isActive", target = "active")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    Trainee toModel(String userName, UpdateTraineeRequest request);


    RegistrationResponse toRegistrationResponse(Trainee trainee);


    @Mapping(source = "active", target = "isActive")
    TraineeProfileResponse toProfileResponse(Trainee trainee);


    TrainerSummaryResponse toTrainerSummaryResponse(Trainer trainer);

    List<TrainerSummaryResponse> toTrainerSummaryResponseList(List<Trainer> trainers);
}