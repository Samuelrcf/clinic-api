package com.samuelrogenes.clinicmanagement.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.samuelrogenes.clinicmanagement.dtos.PacienteDto;
import com.samuelrogenes.clinicmanagement.entities.PacienteEntity;
import com.samuelrogenes.clinicmanagement.exceptions.ResourceAlreadyExistsException;
import com.samuelrogenes.clinicmanagement.exceptions.ResourceNotFoundException;
import com.samuelrogenes.clinicmanagement.mapper.PacienteMapper;
import com.samuelrogenes.clinicmanagement.projections.PacienteProjection;
import com.samuelrogenes.clinicmanagement.repositories.PacienteRepository;
import com.samuelrogenes.clinicmanagement.services.IPacienteService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PacienteService implements IPacienteService {

    private PacienteRepository pacienteRepository;

    @Override
    public PacienteEntity create(PacienteDto pacienteDto) {

        List<PacienteEntity> conflictingPacientes = pacienteRepository.findConflictingPaciente(pacienteDto.getEmail(), pacienteDto.getTelefone(), pacienteDto.getCpf(), pacienteDto.getRg());

        StringBuilder errorMessage = new StringBuilder("Conflito de dados:");

        for (PacienteEntity paciente : conflictingPacientes) {
            if (paciente.getEmail().equals(pacienteDto.getEmail())) {
                errorMessage.append(" Email " + pacienteDto.getEmail() + " já cadastrado.");
            }
            if (paciente.getTelefone().equals(pacienteDto.getTelefone())) {
                errorMessage.append(" Telefone " + pacienteDto.getTelefone() + " já cadastrado.");
            }
            if (paciente.getCpf().equals(pacienteDto.getCpf())) {
                errorMessage.append(" CPF " + pacienteDto.getCpf() + " já cadastrado.");
            }
            if (paciente.getRG().equals(pacienteDto.getRg())) {
            	errorMessage.append(" RG " + pacienteDto.getRg() + " já cadastrado.");
            }
        }

        if (errorMessage.length() > "Conflito de dados:".length()) {
            throw new ResourceAlreadyExistsException(errorMessage.toString());
        }

        PacienteEntity pacienteMapeado = PacienteMapper.mapperToPacienteEntity(new PacienteEntity(), pacienteDto);
        PacienteEntity pacienteSalvo = pacienteRepository.save(pacienteMapeado);

        return pacienteRepository.findById(pacienteSalvo.getId()).orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + pacienteSalvo.getId() + " não encontrado"));
    }

    @Override
    public PacienteProjection findById(Long id) {
        PacienteProjection paciente = pacienteRepository.findPacienteById(id).orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + id + " não foi encontrado"));
        return paciente;
    }

    @Override
    public Page<PacienteProjection> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return pacienteRepository.findAllPacientes(pageable);
    }

    @Override
    public PacienteEntity update(Long id, PacienteDto pacienteDto) {
        PacienteEntity pacienteExistente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + id + " não encontrado"));

        List<PacienteEntity> conflictingPacientes = pacienteRepository.findConflictingPaciente(
                pacienteDto.getEmail(), pacienteDto.getTelefone(), pacienteDto.getCpf(), pacienteDto.getRg());

        StringBuilder errorMessage = new StringBuilder("Conflito de dados:");

        for (PacienteEntity paciente : conflictingPacientes) {
            if (!paciente.getId().equals(id)) {
                boolean emailConflict = paciente.getEmail().equals(pacienteDto.getEmail()) && !paciente.getEmail().equals(pacienteExistente.getEmail());
                boolean telefoneConflict = paciente.getTelefone().equals(pacienteDto.getTelefone()) && !paciente.getTelefone().equals(pacienteExistente.getTelefone());
                boolean cpfConflict = paciente.getCpf().equals(pacienteDto.getCpf()) && !paciente.getCpf().equals(pacienteExistente.getCpf());
                boolean rgConflict = paciente.getRG().equals(pacienteDto.getRg()) && !paciente.getRG().equals(pacienteExistente.getRG());

                if (emailConflict) {
                    errorMessage.append(" Email " + pacienteDto.getEmail() + " já cadastrado.");
                }
                if (telefoneConflict) {
                    errorMessage.append(" Telefone " + pacienteDto.getTelefone() + " já cadastrado.");
                }
                if (cpfConflict) {
                    errorMessage.append(" CPF " + pacienteDto.getCpf() + " já cadastrado.");
                }
                if (rgConflict) {
                    errorMessage.append(" RG " + pacienteDto.getRg() + " já cadastrado.");
                }
            }
        }

        if (errorMessage.length() > "Conflito de dados:".length()) {
            throw new ResourceAlreadyExistsException(errorMessage.toString());
        }

        PacienteMapper.mapperToPacienteEntity(pacienteExistente, pacienteDto);
        PacienteEntity pacienteSalvo = pacienteRepository.save(pacienteExistente);

        return pacienteRepository.findById(pacienteSalvo.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + pacienteSalvo.getId() + " não encontrado"));
    }

    @Override
    public boolean deleteById(Long id) {
        PacienteEntity paciente = pacienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Paciente com ID " + id + " não foi encontrado"));
        pacienteRepository.delete(paciente);
        return true;
    }
}
