package no.novari.flyt.instance.gateway;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.flyt.instance.gateway.kafka.ArchiveCaseIdRequestService;
import no.novari.flyt.instance.gateway.kafka.ArchiveCaseRequestService;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArchiveCaseService {

    private final ArchiveCaseIdRequestService archiveCaseIdRequestService;
    private final ArchiveCaseRequestService archiveCaseRequestService;
    private final SourceApplicationAuthorizationService sourceApplicationAuthorizationService;

    public ArchiveCaseService(
            ArchiveCaseIdRequestService archiveCaseIdRequestService,
            ArchiveCaseRequestService archiveCaseRequestService,
            SourceApplicationAuthorizationService sourceApplicationAuthorizationService
    ) {
        this.archiveCaseIdRequestService = archiveCaseIdRequestService;
        this.archiveCaseRequestService = archiveCaseRequestService;
        this.sourceApplicationAuthorizationService = sourceApplicationAuthorizationService;
    }

    public Optional<SakResource> getCase(
            String archiveCaseId
    ) {
        return archiveCaseRequestService.getByArchiveCaseId(archiveCaseId);
    }

    public Optional<SakResource> getCase(
            Authentication authentication,
            String sourceApplicationInstanceId
    ) {
        return archiveCaseIdRequestService.getArchiveCaseId(
                        sourceApplicationAuthorizationService.getSourceApplicationId(authentication),
                        sourceApplicationInstanceId
                )
                .flatMap(this::getCase);
    }

}
