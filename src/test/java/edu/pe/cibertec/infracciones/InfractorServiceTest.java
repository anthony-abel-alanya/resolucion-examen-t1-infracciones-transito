package edu.pe.cibertec.infracciones;

import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Infractor;
import edu.pe.cibertec.infracciones.repository.InfractorRepository;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.service.impl.InfractorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InfractorServiceTest {

    @Mock
    private InfractorRepository infractorRepository;

    @Mock
    private MultaRepository multaRepository;

    @InjectMocks
    private InfractorServiceImpl infractorService;

    private Infractor infractor;

    @BeforeEach
    void setUp() {
        // Configuración común para todos los tests
        infractor = new Infractor();
        infractor.setId(1L);
        infractor.setBloqueado(false);
    }

    @Test
    @DisplayName("No debe bloquear al infractor cuando tiene 2 multas vencidas y 3 pagadas")
    void verificarBloqueo_noDebeBloquear_cuando2MultasVencidasy3Pagadas() {

        // Configuración específica para este test
        when(infractorRepository.findById(1L)).thenReturn(Optional.of(infractor));
        when(multaRepository.countByInfractor_IdAndEstado(1L, EstadoMulta.VENCIDA)).thenReturn(2L);

        // Acción
        infractorService.verificarBloqueo(1L);

        // Validación
        verify(infractorRepository, never()).save(any());
        assertFalse(infractor.isBloqueado());
    }

    @Test
    @DisplayName("Debe bloquear al infractor cuando tiene 3 multas vencidas")
    void verificarBloqueo_debeBloquear_cuando3MultasVencidas() {

        // Configuración específica para este test
        when(infractorRepository.findById(1L)).thenReturn(Optional.of(infractor));
        when(multaRepository.countByInfractor_IdAndEstado(1L, EstadoMulta.VENCIDA)).thenReturn(3L);

        // Acción
        infractorService.verificarBloqueo(1L);

        // Validación
        verify(infractorRepository).save(infractor);
        assertTrue(infractor.isBloqueado());
    }
}
