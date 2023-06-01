package no.fintlabs.gateway.instance;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.gateway.instance.kafka.ArchiveCaseIdRequestService;
import no.fintlabs.gateway.instance.kafka.ArchiveCaseRequestService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationUtil.getSourceApplicationId;

@Service
public class ArchiveCaseService {

    private final ArchiveCaseIdRequestService archiveCaseIdRequestService;
    private final ArchiveCaseRequestService archiveCaseRequestService;

    public ArchiveCaseService(
            ArchiveCaseIdRequestService archiveCaseIdRequestService,
            ArchiveCaseRequestService archiveCaseRequestService
    ) {
        this.archiveCaseIdRequestService = archiveCaseIdRequestService;
        this.archiveCaseRequestService = archiveCaseRequestService;
    }

    public Optional<SakResource> getCase(String archiveCaseId) {
        return archiveCaseRequestService.getByArchiveCaseId(archiveCaseId);
    }

    public Optional<SakResource> getCase(
            Authentication authentication,
            String sourceApplicationInstanceId
    ) {
        return archiveCaseIdRequestService.getArchiveCaseId(
                        getSourceApplicationId(authentication),
                        sourceApplicationInstanceId
                )
                .flatMap(this::getCase);
    }

}
