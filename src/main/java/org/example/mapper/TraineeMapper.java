package org.example.mapper;


import org.example.dto.request.TraineeRegistrationRequest;
import org.example.dto.request.UpdateTraineeRequest;
import org.example.dto.response.TraineeProfileResponse;
import org.example.model.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.bind.annotation.GetMapping;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    Trainee toModel(TraineeProfileResponse response);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    Trainee toModel(TraineeRegistrationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(source = "isActive", target = "active")
    @Mapping(target = "trainers", ignore = true)
    Trainee toModel(UpdateTraineeRequest request);


}
