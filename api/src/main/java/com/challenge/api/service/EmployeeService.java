package com.challenge.api.service;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeService {

    // This is our temporary persistence layer for the lifetime of the running app.
    private final Map<UUID, EmployeeEntity> employeeStore = new ConcurrentHashMap<>();

    public EmployeeService() {
        seedMockEmployees();
    }

    protected Map<UUID, EmployeeEntity> getEmployeeStore() {
        return employeeStore;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>(employeeStore.values());
        employees.sort(
                Comparator.comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName)
                        .thenComparing(Employee::getUuid));
        return employees;
    }

    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = employeeStore.get(uuid);
        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        return employee;
    }

    public Employee createEmployee(CreateEmployeeRequest request) {
        validateCreateEmployeeRequest(request);

        String firstName = normalizeRequiredTextField(request.getFirstName(), "firstName");
        String lastName = normalizeRequiredTextField(request.getLastName(), "lastName");
        String jobTitle = normalizeRequiredTextField(request.getJobTitle(), "jobTitle");
        String email = normalizeRequiredTextField(request.getEmail(), "email");
        UUID employeeUuid = UUID.randomUUID();
        EmployeeEntity employee =
                new EmployeeEntity(
                        employeeUuid,
                        firstName,
                        lastName,
                        firstName + " " + lastName,
                        request.getSalary(),
                        request.getAge(),
                        jobTitle,
                        email,
                        Instant.now(),
                        null);
        employeeStore.put(employeeUuid, employee);
        return employee;
    }

    private void validateCreateEmployeeRequest(CreateEmployeeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        normalizeRequiredTextField(request.getFirstName(), "firstName");
        normalizeRequiredTextField(request.getLastName(), "lastName");
        normalizeRequiredTextField(request.getJobTitle(), "jobTitle");
        normalizeRequiredTextField(request.getEmail(), "email");

        validatePositiveNumber(request.getSalary(), "salary");
        validatePositiveNumber(request.getAge(), "age");
    }

    private String normalizeRequiredTextField(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private void validatePositiveNumber(Integer value, String fieldName) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        if (value <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be greater than 0");
        }
    }

    private void seedMockEmployees() {
        addMockEmployee(
                new EmployeeEntity(
                        UUID.fromString("2fa6a9f4-d6fc-4d63-8f33-e4fe6e099f59"),
                        "Avery",
                        "Johnson",
                        "Avery Johnson",
                        85000,
                        29,
                        "Software Engineer",
                        "avery.johnson@reliaquest.example",
                        Instant.parse("2022-03-14T09:00:00Z"),
                        null));
        addMockEmployee(
                new EmployeeEntity(
                        UUID.fromString("8fb537a0-5cca-4e85-9a91-6b11ec774996"),
                        "Jordan",
                        "Lee",
                        "Jordan Lee",
                        92000,
                        34,
                        "Security Analyst",
                        "jordan.lee@reliaquest.example",
                        Instant.parse("2021-08-02T09:00:00Z"),
                        null));
        addMockEmployee(
                new EmployeeEntity(
                        UUID.fromString("7f2c9fd8-3053-4f8d-a53a-0baf59416c90"),
                        "Taylor",
                        "Martinez",
                        "Taylor Martinez",
                        78000,
                        27,
                        "Product Specialist",
                        "taylor.martinez@reliaquest.example",
                        Instant.parse("2023-01-09T09:00:00Z"),
                        null));
    }

    private void addMockEmployee(EmployeeEntity employee) {
        employeeStore.put(employee.getUuid(), employee);
    }
}
