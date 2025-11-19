import torch
import numpy as np

def observe(model, epochs, optimizer, loss_fn, train_loader, valid_loader, batch_size, early_stopping, config):
    mean_train_losses = []
    mean_valid_losses = []
    valid_acc_list = []
    #optimizer = torch.optim.Adam(model.parameters(), lr=0.001)
    #loss_fn   = torch.nn.CrossEntropyLoss()

    for epoch in range(epochs):
        train_loss = 0.0
        train_loss2 = 0.0
        valid_loss = 0.0
        valid_loss2 = 0.0

        model.train()

        train_losses = []
        valid_losses = []
        for i, (input, labels) in enumerate(train_loader):

            #model.zero_grad()
            optimizer.zero_grad()
            #print(i)

            outputs = model(input)
            #print("sh", outputs, labels)
            #print("sh", outputs.shape, labels.shape)
            loss_outputs, loss_target = get_loss_inputs(config, outputs, labels)
            #print("sh", outputs.shape, labels.shape)
            #print("sh", outputs, labels)
            loss = loss_fn(loss_outputs, loss_target)

            loss = regularize(config, loss, model)

            loss.backward()
            optimizer.step()
            train_loss += loss.item()
            train_losses.append(loss.item())
            train_loss2 += loss.item() * input.size(0)

            #if (i * batch_size) % (batch_size * 100) == 0:
            #    print(f'{i * batch_size} / 50000')

        model.eval()
        correct = 0
        total = 0
        with torch.no_grad():
          #if False:
            for i, (input, labels) in enumerate(valid_loader):
                #print(i)
                outputs = model(input)
                loss_outputs, loss_target = get_loss_inputs(config, outputs, labels)
                loss = loss_fn(loss_outputs, loss_target)
                outputs_for_loss = outputs
                if False: #config.binary:
                    print("shape", outputs.shape, labels.shape)
                    outputs_for_loss = outputs.reshape(outputs.shape[0])
                    labels = labels.to(dtype=torch.float32).reshape(-1, 1).reshape(outputs.shape[0])
                    print("shape", outputs_for_loss.shape, labels.shape)
                    print("l", labels)
                    print("o", outputs_for_loss)
                    #outputs_for_loss = torch.round(outputs_for_loss)
                    #print("o", outputs_for_loss)
                   # labels = labels.to(torch.float)
                #loss = loss_fn(outputs_for_loss, labels)

                valid_losses.append(loss.item())
                valid_loss += loss.item()
                valid_loss2 += loss.item() * input.size(0)

                _, predicted = torch.max(outputs.data, 1)
                correct += (predicted == labels).sum().item()
                total += labels.size(0)

        mean_train_losses.append(np.mean(train_losses))
        mean_valid_losses.append(np.mean(valid_losses))

        train_loss /= len(train_loader)
        valid_loss /= len(valid_loader)
        train_loss2 /= len(train_loader.dataset)
        valid_loss2 /= len(valid_loader.dataset)
        #val_loss /= len(valid_loader.dataset)
        #print("len valid", len(valid_loader), len(valid_loader.dataset))

        accuracy = 100 * correct / total
        valid_acc_list.append(accuracy)
        print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
              .format(epoch + 1, train_loss2, valid_loss2, accuracy))
        #print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
        #      .format(epoch + 1, train_loss, valid_loss, accuracy))
        #print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
        #      .format(epoch + 1, np.mean(train_losses), np.mean(valid_losses), accuracy))

        torch.cuda.empty_cache()

        # Check early stopping condition
        # early_stopping.check_early_stop(val_loss)

        if early_stopping.stop_training:
            print(f"Early stopping at epoch {i}")
            break


def get_loss_inputs(config, outputs, labels):
    if config.binary:
        outputs = outputs.reshape(outputs.shape[0])
        # labels = labels.to(dtype=torch.float32).reshape(-1, 1).reshape(outputs.shape[0])
        # print("binary")
        labels = labels.to(torch.float)
    return outputs, labels


def regularize(config, loss, model):
    if config.regularize:
        regularization_type = 'L2'
        lambda_reg = 0.01
        # Apply L1 regularization
        if regularization_type == 'L1':
            l1_norm = sum(p.abs().sum() for p in model.parameters())
            loss += lambda_reg * l1_norm

        # Apply L2 regularization
        elif regularization_type == 'L2':
            l2_norm = sum(p.pow(2).sum() for p in model.parameters())
            loss += lambda_reg * l2_norm

        return loss
    return loss


# from github copilot

def observe_new(model, epochs, optimizer, loss_fn, train_loader, valid_loader, batch_size, early_stopping, config):
    """Train/validation loop with correct accuracy computation and device handling.

    Improvements:
    - Move inputs/labels to the model's device.
    - Support binary (sigmoid + threshold) and multiclass (argmax) predictions.
    - Handle labels that are one-hot vectors by taking argmax.
    - Ensure labels have the correct dtype for loss (float for BCE, long for CrossEntropy).
    - Remove noisy debugging prints and make averages robust.
    """

    device = next(model.parameters()).device if any(p is not None for p in model.parameters()) else (torch.device('cuda') if torch.cuda.is_available() else torch.device('cpu'))
    # if True:
    #     import traceback
    #     traceback.print_stack()
    #print("epochs optimizer loss", epochs, optimizer, loss_fn, loss_fn.reduction)
    print("loader", train_loader.dataset.__len__(), valid_loader.dataset.__len__())

    mean_train_losses = []
    mean_valid_losses = []
    valid_acc_list = []

    for epoch in range(epochs):
        model.train()

        train_losses = []
        train_loss_sum = 0.0
        train_examples = 0

        # Training loop
        for i, (inputs, labels) in enumerate(train_loader):
            #if i == 0 and epoch == 0:
            #    print("i", inputs, labels)
            inputs = inputs.to(device)
            labels = labels.to(device)

            optimizer.zero_grad()

            outputs = model(inputs)

            loss_inputs, loss_targets = get_loss_inputs_new(config, outputs, labels)
            #print("l", loss_inputs, loss_targets)
            loss = loss_fn(loss_inputs, loss_targets)
            loss = regularize_new(config, loss, model)

            loss.backward()
            optimizer.step()

            batch_size_effective = inputs.size(0)
            train_losses.append(loss.item())
            train_loss_sum += loss.item() * batch_size_effective
            train_examples += batch_size_effective

        # Validation loop
        model.eval()
        valid_losses = []
        valid_loss_sum = 0.0
        valid_examples = 0
        correct = 0
        total = 0

        with torch.no_grad():
            for i, (inputs, labels) in enumerate(valid_loader):
                #if i == 0 and epoch == 0:
                #   print("i", inputs, labels)
                inputs = inputs.to(device)
                labels = labels.to(device)

                outputs = model(inputs)

                loss_inputs, loss_targets = get_loss_inputs(config, outputs, labels)
                loss = loss_fn(loss_inputs, loss_targets)

                batch_size_effective = inputs.size(0)
                valid_losses.append(loss.item())
                valid_loss_sum += loss.item() * batch_size_effective
                valid_examples += batch_size_effective

                # Prepare labels for accuracy check: convert one-hot to indices if needed
                labels_for_acc = labels
                if labels_for_acc.dim() > 1 and labels_for_acc.size(1) > 1:
                    # one-hot or probability vectors -> take argmax
                    labels_for_acc = torch.argmax(labels_for_acc, dim=1)
                labels_for_acc = labels_for_acc.view(-1).long()

                # Compute predictions according to binary/multiclass
                if getattr(config, 'binary', False):
                    # For binary, accept outputs that may have shape (N,1) or (N,)
                    if i == 0 and epoch == 0:
                        print("outputs", outputs)
                    probs = outputs #torch.sigmoid(outputs)
                    if i == 0 and epoch == 0:
                        print("probs", probs)
                    if probs.dim() > 1 and probs.size(1) == 1:
                        probs = probs.view(-1)
                    preds = (probs > 0.5).long()
                    if i == 0 and epoch == 0:
                        print("preds", preds)
                else:
                    # multiclass logits: take argmax along class dim
                    if outputs.dim() == 1 or (outputs.dim() > 1 and outputs.size(1) == 1):
                        # single-dim outputs -> treat as binary-like; round
                        preds = torch.round(outputs).long().view(-1)
                    else:
                        _, preds = torch.max(outputs, dim=1)

                correct += (preds == labels_for_acc).sum().item()
                total += labels_for_acc.size(0)

        # Compute averaged losses (guard against zero examples)
        avg_train_loss = (train_loss_sum / train_examples) if train_examples > 0 else float('nan')
        avg_valid_loss = (valid_loss_sum / valid_examples) if valid_examples > 0 else float('nan')
        mean_train_losses.append(np.mean(train_losses) if train_losses else float('nan'))
        mean_valid_losses.append(np.mean(valid_losses) if valid_losses else float('nan'))

        accuracy = 100.0 * (correct / total) if total > 0 else 0.0
        valid_acc_list.append(accuracy)

        print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
              .format(epoch + 1, avg_train_loss, avg_valid_loss, accuracy))

        torch.cuda.empty_cache()

        # Check early stopping condition
        if getattr(early_stopping, 'stop_training', False):
            print(f"Early stopping at epoch {epoch + 1}")
            break


def get_loss_inputs_new(config, outputs, labels):
    """Return (outputs_for_loss, labels_for_loss) with correct shapes and dtypes.

    - For binary tasks (config.binary==True): outputs are squeezed to (N,) if needed and
      labels are converted to float.
    - For multiclass tasks: labels are converted to Long tensors of class indices. If labels
      are provided as one-hot vectors, argmax is taken.
    """
    # Ensure labels are on same device as outputs
    labels = labels.to(device=outputs.device)

    if getattr(config, 'binary', False):
        # outputs may be (N,1) or (N,) -> make it (N,)
        if outputs.dim() > 1 and outputs.size(1) == 1:
            outputs = outputs.view(-1)
        labels_proc = labels.view(-1).to(dtype=torch.float)
        return outputs, labels_proc

    # Multiclass: if labels are one-hot/prob vectors, convert to indices
    labels_proc = labels
    if labels_proc.dim() > 1 and labels_proc.size(1) > 1:
        labels_proc = torch.argmax(labels_proc, dim=1)
    labels_proc = labels_proc.view(-1).long()

    return outputs, labels_proc


def regularize_new(config, loss, model):
    if getattr(config, 'regularize', False):
        regularization_type = 'L2'
        lambda_reg = 0.01
        # Apply L1 regularization
        if regularization_type == 'L1':
            l1_norm = sum(p.abs().sum() for p in model.parameters())
            loss = loss + lambda_reg * l1_norm

        # Apply L2 regularization
        elif regularization_type == 'L2':
            l2_norm = sum(p.pow(2).sum() for p in model.parameters())
            loss = loss + lambda_reg * l2_norm

        return loss
    return loss


def _get_device(model):
    try:
        return next(model.parameters()).device
    except StopIteration:
        return torch.device('cuda' if torch.cuda.is_available() else 'cpu')


def get_loss_inputs_new_the_same(config, outputs, labels):
    """Prepare outputs and labels for the loss function.

    For binary tasks (config.binary True):
      - outputs are returned as-is (logits). If outputs shape is (N,1) -> squeezed to (N,)
      - labels are converted to float tensor of shape (N,) for BCEWithLogitsLoss

    For multiclass tasks:
      - outputs are returned as-is (logits of shape (N, C))
      - if labels are one-hot/prob vectors, convert to class indices via argmax
      - labels are converted to long tensors of shape (N,) for CrossEntropyLoss
    """
    # ensure labels on same device as outputs
    labels = labels.to(device=outputs.device)

    if getattr(config, 'binary', False):
        # logits shape handling
        if outputs.dim() > 1 and outputs.size(1) == 1:
            outputs_proc = outputs.view(-1)
        else:
            outputs_proc = outputs
        labels_proc = labels.view(-1).to(dtype=torch.float)
        return outputs_proc, labels_proc

    # multiclass
    labels_proc = labels
    if labels_proc.dim() > 1 and labels_proc.size(1) > 1:
        # assume one-hot or probability vectors
        labels_proc = torch.argmax(labels_proc, dim=1)
    labels_proc = labels_proc.view(-1).to(dtype=torch.long)

    return outputs, labels_proc


def regularize_new2(config, loss, model):
    if not getattr(config, 'regularize', False):
        return loss

    lambda_reg = getattr(config, 'lambda_reg', 0.01)
    reg_type = getattr(config, 'reg_type', 'L2')

    if reg_type == 'L1':
        l1 = sum(p.abs().sum() for p in model.parameters())
        return loss + lambda_reg * l1
    else:
        l2 = sum(p.pow(2).sum() for p in model.parameters())
        return loss + lambda_reg * l2


def observe_new2(model, epochs, optimizer, loss_fn, train_loader, valid_loader, batch_size, early_stopping, config):
    """Simple observe function kept for compatibility.

    Uses device-aware movement and robust averaging/accuracy.
    """
    device = _get_device(model)

    for epoch in range(epochs):
        model.train()
        train_loss_sum = 0.0
        train_examples = 0

        for inputs, labels in train_loader:
            inputs = inputs.to(device)
            labels = labels.to(device)

            optimizer.zero_grad()
            #print(inputs, labels)
            outputs = model(inputs)

            loss_inputs, loss_targets = get_loss_inputs(config, outputs, labels)
            loss = loss_fn(loss_inputs, loss_targets)
            loss = regularize(config, loss, model)

            loss.backward()
            optimizer.step()

            bs = inputs.size(0)
            train_loss_sum += loss.item() * bs
            train_examples += bs

        # validation
        model.eval()
        valid_loss_sum = 0.0
        valid_examples = 0
        correct = 0
        total = 0

        with torch.no_grad():
            for inputs, labels in valid_loader:
                inputs = inputs.to(device)
                labels = labels.to(device)

                outputs = model(inputs)
                loss_inputs, loss_targets = get_loss_inputs(config, outputs, labels)
                loss = loss_fn(loss_inputs, loss_targets)

                bs = inputs.size(0)
                valid_loss_sum += loss.item() * bs
                valid_examples += bs

                # prepare labels for accuracy
                labels_for_acc = labels
                if labels_for_acc.dim() > 1 and labels_for_acc.size(1) > 1:
                    labels_for_acc = torch.argmax(labels_for_acc, dim=1)
                labels_for_acc = labels_for_acc.view(-1).long()

                # predictions
                if getattr(config, 'binary', False):
                    probs = outputs #torch.sigmoid(outputs)
                    if probs.dim() > 1 and probs.size(1) == 1:
                        probs = probs.view(-1)
                    preds = (probs > 0.5).long()
                else:
                    if outputs.dim() == 1 or (outputs.dim() > 1 and outputs.size(1) == 1):
                        preds = torch.round(outputs).long().view(-1)
                    else:
                        _, preds = torch.max(outputs, dim=1)

                correct += (preds == labels_for_acc).sum().item()
                total += labels_for_acc.size(0)

        avg_train_loss = train_loss_sum / train_examples if train_examples > 0 else float('nan')
        avg_valid_loss = valid_loss_sum / valid_examples if valid_examples > 0 else float('nan')
        accuracy = 100.0 * (correct / total) if total > 0 else 0.0

        print(f'epoch: {epoch+1}, train loss: {avg_train_loss:.4f}, valid loss: {avg_valid_loss:.4f}, valid acc: {accuracy:.2f}%')

        torch.cuda.empty_cache()

        if getattr(early_stopping, 'stop_training', False):
            print(f"Early stopping at epoch {epoch+1}")
            break


def observe_new3(model, epochs, optimizer, loss_fn, train_loader, valid_loader, batch_size, early_stopping, config):
    """Improved observe that mirrors observer_another_new behavior but without normalization.

    Kept for compatibility.
    """
    device = _get_device(model)

    for epoch in range(epochs):
        model.train()
        train_loss_sum = 0.0
        train_examples = 0

        for inputs, labels in train_loader:
            inputs = inputs.to(device)
            labels = labels.to(device)

            optimizer.zero_grad()
            outputs = model(inputs)
            #print("O", outputs)

            loss_inputs, loss_targets = get_loss_inputs(config, outputs, labels)
            #print("o2", loss_inputs, loss_targets)
            loss = loss_fn(loss_inputs, loss_targets)
            loss = regularize(config, loss, model)

            loss.backward()
            optimizer.step()

            bs = inputs.size(0)
            train_loss_sum += loss.item() * bs
            train_examples += bs

        # validation
        model.eval()
        valid_loss_sum = 0.0
        valid_examples = 0
        correct = 0
        total = 0

        with torch.no_grad():
            for inputs, labels in valid_loader:
                inputs = inputs.to(device)
                labels = labels.to(device)

                outputs = model(inputs)
                loss_inputs, loss_targets = get_loss_inputs(config, outputs, labels)
                loss = loss_fn(loss_inputs, loss_targets)

                bs = inputs.size(0)
                valid_loss_sum += loss.item() * bs
                valid_examples += bs

                labels_for_acc = labels
                if labels_for_acc.dim() > 1 and labels_for_acc.size(1) > 1:
                    labels_for_acc = torch.argmax(labels_for_acc, dim=1)
                labels_for_acc = labels_for_acc.view(-1).long()

                if getattr(config, 'binary', False):
                    probs = outputs #torch.sigmoid(outputs)
                    if probs.dim() > 1 and probs.size(1) == 1:
                        probs = probs.view(-1)
                    preds = (probs > 0.5).long()
                else:
                    if outputs.dim() == 1 or (outputs.dim() > 1 and outputs.size(1) == 1):
                        preds = torch.round(outputs).long().view(-1)
                    else:
                        _, preds = torch.max(outputs, dim=1)

                correct += (preds == labels_for_acc).sum().item()
                total += labels_for_acc.size(0)

        avg_train_loss = train_loss_sum / train_examples if train_examples > 0 else float('nan')
        avg_valid_loss = valid_loss_sum / valid_examples if valid_examples > 0 else float('nan')
        accuracy = 100.0 * (correct / total) if total > 0 else 0.0

        print(f'epoch: {epoch+1}, train loss: {avg_train_loss:.4f}, valid loss: {avg_valid_loss:.4f}, valid acc: {accuracy:.2f}%')

        torch.cuda.empty_cache()

        if getattr(early_stopping, 'stop_training', False):
            print(f"Early stopping at epoch {epoch+1}")
            break


def observer_another_new(model, epochs, optimizer, loss_fn, train_loader, valid_loader, batch_size, early_stopping, config):
    """Observer that optionally normalizes inputs using training-set statistics and runs robust loops.

    config.normalize: bool (compute train-set mean/std and apply to both train and valid)
    config.binary: bool
    config.regularize: bool
    config.lambda_reg: float (optional)
    config.reg_type: 'L1'|'L2' (optional)
    """
    device = _get_device(model)

    mean = None
    std = None
    if getattr(config, 'normalize', False):
        mean, std = _compute_mean_std(train_loader)
        # move to device
        mean = mean.to(device)
        std = std.to(device)

    for epoch in range(epochs):
        model.train()
        train_loss_sum = 0.0
        train_examples = 0

        for inputs, labels in train_loader:
            if mean is not None and std is not None:
                inputs = _apply_normalize(inputs, mean, std)

            inputs = inputs.to(device)
            labels = labels.to(device)

            optimizer.zero_grad()
            outputs = model(inputs)
            #print(inputs, labels)
            loss_inputs, loss_targets = get_loss_inputs(config, outputs, labels)
            loss = loss_fn(loss_inputs, loss_targets)
            loss = regularize(config, loss, model)

            loss.backward()
            optimizer.step()

            bs = inputs.size(0)
            train_loss_sum += loss.item() * bs
            train_examples += bs

        # validation
        model.eval()
        valid_loss_sum = 0.0
        valid_examples = 0
        correct = 0
        total = 0

        with torch.no_grad():
            for inputs, labels in valid_loader:
                if mean is not None and std is not None:
                    inputs = _apply_normalize(inputs, mean, std)

                inputs = inputs.to(device)
                labels = labels.to(device)

                outputs = model(inputs)
                loss_inputs, loss_targets = get_loss_inputs(config, outputs, labels)
                loss = loss_fn(loss_inputs, loss_targets)

                bs = inputs.size(0)
                valid_loss_sum += loss.item() * bs
                valid_examples += bs

                labels_for_acc = labels
                if labels_for_acc.dim() > 1 and labels_for_acc.size(1) > 1:
                    labels_for_acc = torch.argmax(labels_for_acc, dim=1)
                labels_for_acc = labels_for_acc.view(-1).long()

                if getattr(config, 'binary', False):
                    probs = outputs #torch.sigmoid(outputs)
                    if probs.dim() > 1 and probs.size(1) == 1:
                        probs = probs.view(-1)
                    preds = (probs > 0.5).long()
                else:
                    if outputs.dim() == 1 or (outputs.dim() > 1 and outputs.size(1) == 1):
                        preds = torch.round(outputs).long().view(-1)
                    else:
                        _, preds = torch.max(outputs, dim=1)

                correct += (preds == labels_for_acc).sum().item()
                total += labels_for_acc.size(0)

        avg_train_loss = train_loss_sum / train_examples if train_examples > 0 else float('nan')
        avg_valid_loss = valid_loss_sum / valid_examples if valid_examples > 0 else float('nan')
        accuracy = 100.0 * (correct / total) if total > 0 else 0.0

        print(f'epoch: {epoch+1}, train loss: {avg_train_loss:.4f}, valid loss: {avg_valid_loss:.4f}, valid acc: {accuracy:.2f}%')

        torch.cuda.empty_cache()

        if getattr(early_stopping, 'stop_training', False):
            print(f"Early stopping at epoch {epoch+1}")
            break


def _compute_mean_std(train_loader):
    """Compute per-feature or per-channel mean/std on CPU from the training loader.

    Returns mean, std tensors suitable for broadcasting with input batches.
    """
    sum_ = None
    sumsq_ = None
    count = 0

    for inputs, _ in train_loader:
        x = inputs.detach().cpu().double()
        if x.dim() == 1:
            # (N,) treat as single feature
            s = x.sum(dim=0)
            s2 = (x * x).sum(dim=0)
            n = x.shape[0]
            if sum_ is None:
                sum_ = s
                sumsq_ = s2
            else:
                sum_ = sum_ + s
                sumsq_ = sumsq_ + s2
            count += n
        elif x.dim() == 2:
            # (N, D) per-feature
            N = x.size(0)
            s = x.sum(dim=0)
            s2 = (x * x).sum(dim=0)
            if sum_ is None:
                sum_ = s
                sumsq_ = s2
            else:
                sum_ = sum_ + s
                sumsq_ = sumsq_ + s2
            count += N
        else:
            # (N, C, ...)
            N = x.size(0)
            C = x.size(1)
            # flatten spatial dims
            rest = int(torch.prod(torch.tensor(x.size()[2:]))) if x.dim() > 2 else 1
            tmp = x.view(N, C, -1)
            s = tmp.sum(dim=0).sum(dim=1)  # shape (C,)
            s2 = (tmp * tmp).sum(dim=0).sum(dim=1)
            if sum_ is None:
                sum_ = s
                sumsq_ = s2
            else:
                sum_ = sum_ + s
                sumsq_ = sumsq_ + s2
            count += N * rest

    if sum_ is None or count == 0:
        # fallback: no normalization
        return torch.tensor(0.0), torch.tensor(1.0)

    mean = (sum_ / count).float()
    var = (sumsq_ / count).float() - mean * mean
    var = torch.clamp(var, min=0.0)
    std = torch.sqrt(var)
    if std.numel() == 1:
        if std.item() == 0:
            std = torch.tensor(1.0)
    else:
        std[std == 0] = 1.0

    return mean, std


def _apply_normalize(x, mean, std):
    """Apply normalization (x - mean) / std to batch x. mean/std should be on same device as x.

    Supports batch shapes: (N,), (N, D), (N, C, H, W), etc.
    """
    # convert to float, apply, then cast back
    orig_dtype = x.dtype
    x = x.to(dtype=torch.float32)

    if x.dim() == 1:
        return ((x - mean) / std).to(dtype=orig_dtype)
    elif x.dim() == 2:
        m = mean.view(1, -1)
        s = std.view(1, -1)
        return ((x - m) / s).to(dtype=orig_dtype)
    else:
        # assume channel dim = 1
        shape = [1, -1] + [1] * (x.dim() - 2)
        m = mean.view(*shape).to(device=x.device)
        s = std.view(*shape).to(device=x.device)
        return ((x - m) / s).to(dtype=orig_dtype)


def print_state_dict(model, optimizer):
    # Print model's state_dict
    print("Model's state_dict:")
    for param_tensor in model.state_dict():
        print(param_tensor, "\t", model.state_dict()[param_tensor].size())

    # Print optimizer's state_dict
    print("Optimizer's state_dict:")
    for var_name in optimizer.state_dict():
        print(var_name, "\t", optimizer.state_dict()[var_name])


def print_state_dict_alt(model, optimizer):
    print("Model's state_dict:")
    for param_tensor in model.state_dict():
        print(param_tensor, "\t", model.state_dict()[param_tensor].size())

    print("Optimizer's state_dict:")
    for var_name in optimizer.state_dict():
        print(var_name, "\t", optimizer.state_dict()[var_name])


def print_model_parameters(model):
    print("Model parameters")
    for name, param in model.named_parameters():
        print(name, param.shape)

