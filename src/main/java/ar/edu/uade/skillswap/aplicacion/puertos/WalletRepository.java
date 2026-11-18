package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.comun.RecursoNoEncontradoException;
import ar.edu.uade.skillswap.dominio.wallet.Wallet;

import java.util.Optional;

public interface WalletRepository {

    Optional<Wallet> porPerfil(String perfilId);

    void guardar(Wallet wallet);

    default Wallet obtener(String perfilId) {
        return porPerfil(perfilId).orElseThrow(() -> new RecursoNoEncontradoException("Wallet", perfilId));
    }
}
