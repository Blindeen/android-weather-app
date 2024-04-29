package com.project.weatherapp.dto.forecast;

import java.util.List;

public class ForecastResponseDto {
    private List<SingleTimestampDto> list;

    public List<SingleTimestampDto> getList() {
        return list;
    }
}
