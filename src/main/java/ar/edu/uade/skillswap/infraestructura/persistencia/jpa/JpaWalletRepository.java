package ar.edu.uade.skillswap.infraestructura.persistencia.jpa;

import ar.edu.uade.skillswap.aplicacion.puertos.WalletRepository;
import ar.edu.uade.skillswap.dominio.wallet.Wallet;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaWalletRepository extends RepositorioJpa<Wallet> implements WalletRepository {

    public JpaWalletRepository() {
        super(Wallet.class);
    }

    @Override
    public Optional<Wallet> porPerfil(String perfilId) {
        return Optional.ofNullable(buscar(perfilId));
    }

    @Override
    public void guardar(Wallet wallet) {
        guardarEntidad(wallet, wallet.getPerfilId());
    }
}
