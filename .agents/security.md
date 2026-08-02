# Agent Reference — Security

Read this file when touching anything that accepts external input (packets, bundle scripts, server commands).

## Data Validation

- Validate all inputs from packets.
- Sanitize bundle scripts.
- Use packet bounds for network safety.

## Access Control

- Use `@ApiStatus.Internal` for internal APIs.
- Avoid exposing sensitive functionality in public API.
- Validate permissions for server-side operations.

## Resource Limits

- Limit bundle script execution time.
- Cap packet sizes.
- Prevent memory leaks in event handlers.
