package org.example.mapper;

import org.example.dto.request.TrainerRegistrationRequest;
import org.example.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    Trainer toModel(TrainerRegistrationRequest request);
}
