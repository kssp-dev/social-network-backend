package ru.skillbox.socialnet.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.skillbox.socialnet.dto.response.WeatherRs;
import ru.skillbox.socialnet.entity.locationrelated.Weather;

@Mapper(componentModel = "spring")
public interface WeatherMapper {
    @SuppressWarnings("unused")
    WeatherMapper INSTANCE = Mappers.getMapper(WeatherMapper.class);

    WeatherRs toRs(Weather weather);
}
