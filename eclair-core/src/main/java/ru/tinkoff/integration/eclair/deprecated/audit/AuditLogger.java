package ru.tinkoff.integration.eclair.deprecated.audit;

import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: refactor
 */
//@Component
public class AuditLogger /*extends Logger*/ {

    private final InputResolver inputResolver;
//    private final AuditService auditService;
    private final PayloadPreparator payloadPreparator;

    @Autowired
    public AuditLogger(InputResolver inputResolver,
//                       AuditService auditService,
                       PayloadPreparator payloadPreparator) {
        this.inputResolver = inputResolver;
//        this.auditService = auditService;
        this.payloadPreparator = payloadPreparator;
    }

    /**
     * TODO: process 'message'
     */
    public void logInput(JoinPoint joinPoint, Class<?>[] wrappers, Class<?> inputClass, String[] mask) {
        /*org.slf4j.Logger logger = getLogger(joinPoint);
        if (isAuditEnabled(logger)) {
            Object input = inputResolver.resolve(joinPoint, inputClass);
            if (nonNull(input)) {
                String callCode = buildPoint(joinPoint, Event.IN);
                String message = callCode;
                Class<?> wrapper = wrappers.length == 0 ? null : wrappers[0];
                String inputString = payloadPreparator.prepare(input, wrapper, mask);
//                auditService.logString(getIntegrationId(), callCode, message, inputString, logger.getName(), getOperationName());
            }
        }*/
    }

    /**
     * TODO: process 'message'
     */
    public void logOutput(JoinPoint joinPoint, Object output, Class<?>[] wrappers, String[] mask) {
        /*org.slf4j.Logger logger = getLogger(joinPoint);
        if (isAuditEnabled(logger)) {
            String callCode = buildPoint(joinPoint, Event.OUT);
            String message = callCode;
            Class<?> wrapper = wrappers.length < 2 ? null : wrappers[1];
            String outputString = payloadPreparator.prepare(output, wrapper, mask);
//            auditService.logString(getIntegrationId(), callCode, message, outputString, logger.getName(), getOperationName());
        }*/
    }
}
