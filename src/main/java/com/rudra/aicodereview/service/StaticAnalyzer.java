package com.rudra.aicodereview.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StaticAnalyzer {

    public record StaticIssue(String type, String message, int line, int startIndex, int endIndex) {}

    public List<StaticIssue> analyze(String code) {
        List<StaticIssue> issues = new ArrayList<>();
        if (code == null || code.isEmpty()) return issues;

        String[] lines = code.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            
            // 1. SQL string concatenation
            if (line.matches(".*(?i)(select|insert|update|delete|create|drop)\\s+.*\\+.*")) {
                if (!line.contains("Logger") && !line.contains("log.")) {
                    issues.add(new StaticIssue("SQL_INJECTION", "Potential SQL string concatenation detected.", lineNumber, code.indexOf(line), code.indexOf(line) + line.length()));
                }
            }
            
            // 2. Thread unsafe collections (e.g., non-concurrent collection in static context)
            if (line.matches(".*static.*(HashMap|ArrayList|HashSet).*<.*=.*new.*")) {
                issues.add(new StaticIssue("THREAD_SAFETY", "Thread unsafe collection used in static context.", lineNumber, code.indexOf(line), code.indexOf(line) + line.length()));
            }

            // 3. Division by zero
            if (line.matches(".*\\/\\s*0\\b.*")) {
                issues.add(new StaticIssue("DIV_ZERO", "Division by zero detected.", lineNumber, code.indexOf(line), code.indexOf(line) + line.length()));
            }

            // 4. Hardcoded credentials
            if (line.matches(".*(?i)(password|secret|key|token)\\s*=\\s*[\"'].+[\"'].*")) {
                issues.add(new StaticIssue("HARDCODED_CREDENTIALS", "Potential hardcoded credentials detected.", lineNumber, code.indexOf(line), code.indexOf(line) + line.length()));
            }
            
            // 5. Array index errors (simple out of bounds like arr[arr.length])
            if (line.matches(".*\\[\\s*[a-zA-Z0-9_]+\\.length\\s*\\].*") || line.matches(".*<=\\s*[a-zA-Z0-9_]+\\.length.*")) {
                issues.add(new StaticIssue("ARRAY_INDEX", "Potential array index out of bounds error.", lineNumber, code.indexOf(line), code.indexOf(line) + line.length()));
            }
        }
        
        // 6. File resources not closed
        if (code.contains("new FileInputStream") || code.contains("new FileOutputStream")) {
            if (!code.contains("try (") && !code.contains("close()")) {
                issues.add(new StaticIssue("RESOURCE_LEAK", "File resource potentially not closed.", 1, 0, Math.min(code.length(), 100)));
            }
        }

        return issues;
    }
}
