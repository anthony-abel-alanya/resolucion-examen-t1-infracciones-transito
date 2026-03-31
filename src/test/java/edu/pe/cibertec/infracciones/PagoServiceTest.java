package edu.pe.cibertec.infracciones;


import edu.pe.cibertec.infracciones.dto.PagoResponseDTO;
import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.model.Pago;
import edu.pe.cibertec.infracciones.repository.InfractorRepository;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.repository.PagoRepository;
import edu.pe.cibertec.infracciones.service.impl.MultaServiceImpl;
import edu.pe.cibertec.infracciones.service.impl.PagoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MultaRepository multaRepository;

    @InjectMocks
    private PagoServiceImpl pagoService;

    @Test
    @DisplayName("Procesar pago aplica descuento del 20% si se paga el mismo día")
    void procesarPago_aplicaDescuentoYActualizaEstado_mismoDia() {

        // Preparación
        Multa multa = new Multa();
        multa.setId(1L);
        multa.setMonto(500.0);
        multa.setFechaEmision(LocalDate.now());
        multa.setFechaVencimiento(LocalDate.now().plusDays(15));
        multa.setEstado(EstadoMulta.PENDIENTE);

        when(multaRepository.findById(1L))
                .thenReturn(Optional.of(multa));

        // Ejecución
        PagoResponseDTO pago = pagoService.procesarPago(1L);

        // Verificación del monto con descuento
        double montoEsperado = 500.0 * 0.8;
        assertEquals(montoEsperado, pago.getMontoPagado());

        // Verificación del estado de la multa
        assertEquals(EstadoMulta.PAGADA, multa.getEstado());

        // Verificación de persistencia
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    @DisplayName("Debe aplicar recargo del 15% cuando la multa está vencida")
    void procesarPago_debeAplicarRecargo_cuandoMultaEstaVencida() {

        // Dado: multa vencida (sin descuento, con recargo)
        Multa multa = new Multa();
        multa.setId(1L);
        multa.setMonto(500.00);
        multa.setFechaEmision(LocalDate.now().minusDays(12));
        multa.setFechaVencimiento(LocalDate.now().minusDays(2));
        multa.setEstado(EstadoMulta.PENDIENTE);

        when(multaRepository.findById(1L))
                .thenReturn(Optional.of(multa));

        // Cuando
        pagoService.procesarPago(1L);

        // Entonces: capturamos el pago guardado
        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);

        verify(pagoRepository, times(1)).save(captor.capture());

        Pago pagoCapturado = captor.getValue();

        // Validaciones
        assertEquals(0.0, pagoCapturado.getDescuentoAplicado(), 0.001);
        assertEquals(75.0, pagoCapturado.getRecargo(), 0.001);
        assertEquals(575.0, pagoCapturado.getMontoPagado(), 0.001);
    }
}
