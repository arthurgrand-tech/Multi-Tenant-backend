package com.ArthurGrand.module.Client.serviceImp;

import com.ArthurGrand.common.exception.ClientNotFoundException;
import com.ArthurGrand.dto.ClientDTO;
import com.ArthurGrand.module.Client.entity.Client;
import com.ArthurGrand.module.Client.repository.ClientRepository;
import com.ArthurGrand.module.Client.service.ClientService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImp implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    public ClientServiceImp(ClientRepository clientRepository, ModelMapper modelMapper) {
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ClientDTO saveClient(ClientDTO clientDTO) {
        // Check if email already exists
        if (clientDTO.getEmail() != null && clientRepository.existsByEmail(clientDTO.getEmail())) {
            throw new IllegalArgumentException("Client with email " + clientDTO.getEmail() + " already exists");
        }

        Client client = modelMapper.map(clientDTO, Client.class);
        Client savedClient = clientRepository.save(client);
        return modelMapper.map(savedClient, ClientDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientById(Integer id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    public ClientDTO updateClient(Integer id, ClientDTO clientDTO) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        // Check if email is being changed and if new email already exists
        if (clientDTO.getEmail() != null &&
                !clientDTO.getEmail().equals(existingClient.getEmail()) &&
                clientRepository.existsByEmail(clientDTO.getEmail())) {
            throw new IllegalArgumentException("Client with email " + clientDTO.getEmail() + " already exists");
        }

        // Update fields
        existingClient.setClientName(clientDTO.getClientName());
        existingClient.setEmail(clientDTO.getEmail());
        existingClient.setPhone(clientDTO.getPhone());
        existingClient.setAddress(clientDTO.getAddress());
        existingClient.setWebsite(clientDTO.getWebsite());
        existingClient.setContactPerson(clientDTO.getContactPerson());
        existingClient.setIndustry(clientDTO.getIndustry());
        if (clientDTO.getIsActive() != null) {
            existingClient.setIsActive(clientDTO.getIsActive());
        }

        Client updatedClient = clientRepository.save(existingClient);
        return modelMapper.map(updatedClient, ClientDTO.class);
    }

    @Override
    public void deleteClient(Integer id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(id);
        }
        clientRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> getActiveClients() {
        return clientRepository.findAllActiveClients().stream()
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> searchClients(String clientName, String email, String industry, Boolean isActive) {
        return clientRepository.findClientsWithFilters(clientName, email, industry, isActive).stream()
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ClientDTO activateClient(Integer id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        client.setIsActive(true);
        Client updatedClient = clientRepository.save(client);
        return modelMapper.map(updatedClient, ClientDTO.class);
    }

    @Override
    public ClientDTO deactivateClient(Integer id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        client.setIsActive(false);
        Client updatedClient = clientRepository.save(client);
        return modelMapper.map(updatedClient, ClientDTO.class);
    }
}