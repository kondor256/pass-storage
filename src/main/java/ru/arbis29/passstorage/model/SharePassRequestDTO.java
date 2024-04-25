package ru.arbis29.passstorage.model;

import lombok.Data;

import java.util.List;

@Data
public class SharePassRequestDTO {
    String passId;
    List<String> userIdList;
}
