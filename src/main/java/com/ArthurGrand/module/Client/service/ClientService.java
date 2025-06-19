package com.ArthurGrand.module.Client.service;

import com.ArthurGrand.dto.ClientDTO;

import java.util.List;

public interface ClientService {

    ClientDTO saveClient(ClientDTO clientDTO);

    List<ClientDTO> getAllClients();

    ClientDTO getClientById(Integer id);

    ClientDTO updateClient(Integer id, ClientDTO clientDTO);

    void deleteClient(Integer id);

    List<ClientDTO> getActiveClients();

    List<ClientDTO> searchClients(String clientName, String email, String industry, Boolean isActive);

    ClientDTO activateClient(Integer id);

    ClientDTO deactivateClient(Integer id);
}