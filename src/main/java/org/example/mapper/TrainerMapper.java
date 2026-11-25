package org.example.mapper;

import org.example.dto.request.TrainerRegistrationRequest;
import org.example.dto.request.UpdateTrainerRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.dto.response.TraineeSummaryResponse;
import org.example.dto.response.TrainerProfileResponse;
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
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    Trainer toModel(TrainerRegistrationRequest request);


    @Mapping(source = "request.firstName", target = "firstName")
    @Mapping(source = "request.lastName", target = "lastName")
    @Mapping(source = "request.isActive", target = "active")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    Trainer toModel(String userName, UpdateTrainerRequest request);


    RegistrationResponse toRegistrationResponse(Trainer trainer);


    @Mapping(source = "active", target = "isActive")
    TrainerProfileResponse toProfileResponse(Trainer trainer);


    TraineeSummaryResponse toTraineeSummaryResponse(Trainee trainee);

    List<TraineeSummaryResponse> toTraineeSummaryResponseList(List<Trainee> trainees);
}