# Correção comprovada na configuração do runner

Na execução inicial 38473475479 (commit c55c446), build, integração e publicação passaram. O smoke test de staging retornou 401 no dashboard autenticado, e produção foi ignorada pela dependência de staging.

Os Secrets vazios declarados no ambiente do job tinham precedência sobre deploy.env na interpolação do Compose. Com isso, a aplicação usava as senhas locais de fallback enquanto o smoke test usava a senha aleatória gerada.

A correção exporta os mesmos cinco valores gerados para GITHUB_ENV, preservando as máscaras dos logs. Assim, os próximos passos recebem as mesmas credenciais do deploy.env e do smoke test. Nenhuma senha foi fixada para produção.

Uma reprodução com docker compose config confirmou a divergência anterior e a consistência após alinhar o ambiente do processo, para API e Oracle. A execução completa posterior valida o comportamento no GitHub.
